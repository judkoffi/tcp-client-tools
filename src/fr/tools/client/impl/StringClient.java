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
    private ByteBuffer buffer;
    private final Charset charset;

    private StringPacket(String value, Charset charset) {
      this.buffer = charset.encode(value).compact();
      this.charset = charset;
    }

    private StringPacket(ByteBuffer bb, Charset charset) {
      this.buffer = bb.compact();
      this.charset = charset;
    }

    private StringPacket(String value) {
      this(value, StandardCharsets.UTF_8);
    }

    private StringPacket(ByteBuffer bb) {
      this(bb, StandardCharsets.UTF_8);
    }

    public static StringPacket from(String value) {
      return new StringPacket(value);
    }

    public static StringPacket from(ByteBuffer buffer) {
      return new StringPacket(buffer);
    }

    @Override
    public ByteBuffer toBuffer() {
      var bb = ByteBuffer.allocate(Integer.BYTES + buffer.capacity());
      bb.putInt(buffer.capacity());
      bb.put(buffer);
      return bb;
    }

    @Override
    public String getValue() {
      buffer.flip();
      var size = buffer.getInt();
      var value = charset.decode(buffer).toString();
      buffer.clear();
      return value;
    }
  }

  private final Charset charset;
  private final Thread reader;
  private final Thread write;
  private final static int SLEEP = 100;
  private SocketChannel channel;
  private ThreadLocalRandom random;
  private final int size;
  private final static int BUFER_SIZE = 1024;
  private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFER_SIZE);

  public StringClient(InetSocketAddress servAddr, int size, Charset charset) throws IOException {
    this.charset = charset;
    this.reader = new Thread(this::readPacket);
    this.write = new Thread(this::sendPacket);
    this.random = ThreadLocalRandom.current();
    this.size = size;
    this.channel = SocketChannel.open(servAddr);
  }

  public StringClient(InetSocketAddress servAddr, int size) throws IOException {
    this(servAddr, size, StandardCharsets.UTF_8);
  }

  @Override
  public IPacket<String> buildPacket(String elt) {
    return StringPacket.from(elt);
  }

  @Override
  public IPacket<String> buildPacket(ByteBuffer buffer) {
    return StringPacket.from(buffer);
  }

  @Override
  public void sendPacket() {
    for (var i = 0; i < size; i++) {
      String value = generateString(1 + random.nextInt(BUFER_SIZE) % (BUFER_SIZE - Integer.BYTES));
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

  private static boolean readFully(SocketChannel sc, ByteBuffer bb) throws IOException {
    while (bb.hasRemaining()) {
      int read = sc.read(bb);
      if (read == -1)
        return false;
    }
    return true;
  }

  @Override
  public void readPacket() {
    for (var i = 0; i < size; i++) {
      try {
        readBuffer.clear();
        readFully(channel, readBuffer);
        System.out.println(readBuffer.remaining());
        var packet = buildPacket(readBuffer);
        System.out.println("value: " + packet.getValue());
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
    reader.join();
    write.join();
    channel.close();
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
