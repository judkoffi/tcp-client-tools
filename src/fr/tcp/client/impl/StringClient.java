package fr.tcp.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.IClient;
import fr.tcp.client.IPacket;

public class StringClient extends AbstractClient<String> {

  private static class StringPacket implements IPacket<String> {
    private final Charset charset;
    private final static String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private final static String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private final static String NUMBER = "0123456789";
    private final static String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;

    private StringPacket(Charset charset) {
      this.charset = charset;
    }

    @Override
    public ByteBuffer getRandomPacket() {
      String value = generateRandomString(10);
      ByteBuffer encoded = charset.encode(value);
      int encodedValueLength = encoded.limit();
      ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + encodedValueLength);
      bb.putInt(encodedValueLength);
      bb.put(encoded);
      bb.flip();
      return bb;
    }

    @Override
    public String getValueFrom(ByteBuffer bb) throws IOException {
      bb.flip();
      String str = charset.decode(bb).toString();
      bb.compact();
      return str;
    }

    private static String generateRandomString(int length) {
      var random = ThreadLocalRandom.current();
      if (length < 1)
        throw new IllegalArgumentException(length + " must be gretther than 0");

      var sb = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
        int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
        char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
        sb.append(rndChar);
      }
      return sb.toString();
    }
  }

  private final static int BUFFER_SIZE = 1024;
  private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

  public StringClient(InetSocketAddress servAddr, IPacket<String> packetBuilder, int size,
      int timeout) throws IOException {
    super(servAddr, packetBuilder, size, timeout);
  }

  @Override
  public void readPacket() {
    for (var i = 0; i < size; i++) {
      try {
        var sizeBuff = ByteBuffer.allocate(Integer.BYTES);
        buffer.clear();
        IClient.readFully(channel, sizeBuff);
        int size = sizeBuff.flip().getInt();
        buffer.limit(size);
        IClient.readFully(channel, buffer);
        System.out.println("value: " + packetBuilder.getValueFrom(buffer));
        Thread.sleep(timeout * 2);
      } catch (IOException e) {
        System.out.println(e);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 5) {
      System.err.println("Usage: java StringClient addr port nbElements timeout charset");
      return;
    }

    InetSocketAddress server = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    var size = Integer.valueOf(args[2]);
    var timeout = Integer.valueOf(args[3]);
    var charsetName = args[4];
    Charset charset = Charset.isSupported(charsetName) //
        ? Charset.forName(charsetName)
        : StandardCharsets.UTF_8;

    var packetBuilder = new StringPacket(charset);
    var client = new StringClient(server, packetBuilder, size, timeout);
    client.launch();
    client.free();
  }

}
