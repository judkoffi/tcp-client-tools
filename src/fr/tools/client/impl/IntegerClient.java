package fr.tools.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadLocalRandom;
import fr.tools.client.IClient;
import fr.tools.client.IPacket;
import fr.tools.client.packet.IntegerPacket;

public class IntegerClient implements IClient<Integer> {
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
        var packet = IntegerPacket.from(buffer);
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
  public void free() {
    reader.interrupt();
    write.interrupt();
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println("Usage: java IntegerClient addr port size");
      return;
    }

    InetSocketAddress server = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    var client = new IntegerClient(server, Integer.valueOf(args[2]));
    client.launch();
    // client.free();
  }
}
