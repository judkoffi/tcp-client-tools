package fr.tools.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;
import fr.tools.client.IClient;
import fr.tools.client.IPacket;

public class IntegerClient implements IClient<Integer> {

  public static class IntegerPacket implements IPacket<Integer> {
    private ByteBuffer buffer;

    private IntegerPacket(int value) {
      this.buffer = ByteBuffer.allocate(Integer.BYTES);
      this.buffer.putInt(value);
    }

    private IntegerPacket(ByteBuffer buffer) {
      this.buffer = buffer;
    }

    public static IntegerPacket from(Integer value) {
      return new IntegerPacket(value);
    }

    public static IntegerPacket from(ByteBuffer buffer) {
      return new IntegerPacket(buffer);
    }

    @Override
    public ByteBuffer toBuffer() {
      return buffer.duplicate();
    }

    @Override
    public Integer getValue() {
      buffer.flip();
      int value = buffer.getInt();
      buffer.clear();
      return value;
    }
  }


  private final Thread reader;
  private final Thread write;
  private final static int SLEEP = 100;
  private SocketChannel channel;
  private ThreadLocalRandom random;
  private final int size;

  public IntegerClient(InetSocketAddress servAddr, int size) throws IOException {
    this.reader = new Thread(this::readPacket);
    this.write = new Thread(this::sendPacket);
    this.random = ThreadLocalRandom.current();
    this.size = size;
    this.channel = SocketChannel.open(servAddr);
  }

  @Override
  public IPacket<Integer> buildPacket(Integer value) {
    return IntegerPacket.from(value);
  }

  @Override
  public IPacket<Integer> buildPacket(ByteBuffer buffer) {
    return IntegerPacket.from(buffer);
  }

  @Override
  public void sendPacket() {
    for (var i = 0; i < size; i++) {
      int value = random.nextInt();
      try {
        channel.write(buildPacket(value).toBuffer().flip());
        Thread.sleep(SLEEP / 2);
      } catch (IOException e) {
        System.out.println(e);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  @Override
  public void readPacket() {
    for (var i = 0; i < size; i++) {
      try {
        var buffer = ByteBuffer.allocate(Integer.BYTES);
        channel.read(buffer);
        var packet = buildPacket(buffer);
        System.out.println("value: " + packet.getValue());
        Thread.sleep(SLEEP);
      } catch (IOException e) {
        System.out.println(e);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  @Override
  public void launch() {
    write.start();
    reader.start();
  }

  @Override
  public void free() throws IOException, InterruptedException {
    reader.join();
    write.join();
    channel.close();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 3) {
      System.err.println("Usage: java IntegerClient addr port size");
      return;
    }

    InetSocketAddress server = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    var client = new IntegerClient(server, Integer.valueOf(args[2]));
    client.launch();
    client.free();
  }


}
