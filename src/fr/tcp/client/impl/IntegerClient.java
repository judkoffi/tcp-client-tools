package fr.tcp.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.IClient;
import fr.tcp.client.IPacket;

public class IntegerClient extends AbstractClient<Integer> {

  private static class IntegerPacket implements IPacket<Integer> {
    private final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public ByteBuffer getRandomPacket() {
      buffer.clear();
      buffer.putInt(random.nextInt(Integer.MAX_VALUE));
      return buffer.flip();
    }

    @Override
    public Integer getValueFrom(ByteBuffer bb) {
      bb.flip();
      int value = bb.getInt();
      bb.compact();
      return value;
    }

    @Override
    public ByteBuffer getBoundedRandomPacket(int lenght) {
      buffer.clear();
      buffer.putInt(random.nextInt(lenght));
      return buffer.flip();
    }
  }

  private ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

  public IntegerClient(InetSocketAddress servAddr, IPacket<Integer> packetBuilder, int size,
      int timeout, Integer valueLimit) throws IOException {
    super(servAddr, packetBuilder, size, timeout, valueLimit);
  }

  @Override
  public void readPacket() {
    for (var i = 0; i < size; i++) {
      try {
        buffer.clear();
        IClient.readFully(channel, buffer);
        var value = packetBuilder.getValueFrom(buffer);
        System.out.println("value: " + value);
        Thread.sleep(timeout * 2);
      } catch (IOException e) {
        System.out.println(e);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length < 4) {
      System.err.println("Usage: java IntegerClient addr port numberOfElements timeout [maxValue]");
      return;
    }

    System.out.println(Arrays.deepToString(args));
    InetSocketAddress server = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    var size = Integer.valueOf(args[2]);
    var timeout = Integer.valueOf(args[3]);

    System.out.println("val :" + args[4]);


    var valueLimit = parseInt(args[4]).orElse(Integer.MAX_VALUE);
    var packetBuilder = new IntegerPacket();

    IntegerClient client = new IntegerClient(server, packetBuilder, size, timeout, valueLimit);
    client.launch();
    client.free();
  }
}
