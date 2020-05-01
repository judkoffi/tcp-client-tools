package fr.tools.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import fr.tools.client.IClient;
import fr.tools.client.IPacket;

public class StringClient implements IClient<String> {

  private static class StringPacket implements IPacket<String> {
    private final Charset charset;
    private SocketChannel channel;

    private StringPacket(SocketChannel channel, Charset charset) {
      this.charset = charset;
      this.channel = channel;
    }

    private StringPacket(SocketChannel channel) {
      this(channel, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer getRandomPacket(String value) {
      var encoded = charset.encode(value);
      var buffer = ByteBuffer.allocate(BUFFER_SIZE);
      buffer.putInt(encoded.capacity());
      buffer.put(encoded);
      return buffer;
    }

    @Override
    public String getValueFrom(ByteBuffer bb) throws IOException {
      var sizeBuff = ByteBuffer.allocate(Integer.BYTES);
      IClient.readFully(channel, sizeBuff);
      int size = sizeBuff.flip().getInt();
      System.out.println("size: " + size);
      bb.limit(size);
      IClient.readFully(channel, bb);



      bb.flip();
      // int size = bb.getInt();
      // bb.limit(size);
      String result = charset.decode(bb).toString();
      bb.compact();
      return result;
    }
  }

  private final Thread reader;
  private final Thread write;
  private final static int SLEEP = 1;
  private SocketChannel channel;
  private ThreadLocalRandom random;
  private final int size;
  private final static int BUFFER_SIZE = 1024;
  private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
  private final IPacket<String> builder;

  public StringClient(InetSocketAddress servAddr, int size, Charset charset) throws IOException {
    this.reader = new Thread(this::readPacket);
    this.write = new Thread(this::sendPacket);
    this.random = ThreadLocalRandom.current();
    this.size = size;
    this.channel = SocketChannel.open(servAddr);
    this.builder = new StringPacket(channel,charset);
  }

  public StringClient(InetSocketAddress servAddr, int size) throws IOException {
    this(servAddr, size, StandardCharsets.UTF_8);
  }

  @Override
  public void sendPacket() {
    for (var i = 0; i < size; i++) {
      var length = 1 + random.nextInt(BUFFER_SIZE) % (BUFFER_SIZE - Integer.BYTES);
      String value = generateString(5);
      try {
        var bb = builder.getRandomPacket(value).flip();
        channel.write(bb);
        Thread.sleep(0);
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
        var sizeBuff = ByteBuffer.allocate(Integer.BYTES);
        readBuffer.clear();
        IClient.readFully(channel, sizeBuff);
        int size = sizeBuff.flip().getInt();
        System.out.println("size: " + size);
        readBuffer.limit(size);
        IClient.readFully(channel, readBuffer);
        var value = builder.getValueFrom(readBuffer);
        System.out.println("value: " + value);
        Thread.sleep(SLEEP);
      } catch (IOException e) {
        System.out.println(e);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  public void launch() {
    write.start();
    reader.start();
  }

  @Override
  public void free() throws IOException, InterruptedException {
    // write.join();
    // reader.join();
    // channel.close();
  }



  public String generateString(int length) {
    char leftLimit = '0'; // numeral '0'
    char rightLimit = 'z'; // letter 'z'

    return random
      .ints(leftLimit, rightLimit + 1)
      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
      .limit(length)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 3) {
      System.err.println("Usage: java StringClient addr port size");
      return;
    }

    InetSocketAddress server = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    var client = new StringClient(server, Integer.valueOf(args[2]), StandardCharsets.UTF_8);
    client.launch();
    client.free();
  }
}
