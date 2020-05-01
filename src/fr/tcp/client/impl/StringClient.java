package fr.tcp.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.Helper;
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

    private ByteBuffer getPacket(int length) {
      String value = generateRandomString(length);
      ByteBuffer encoded = charset.encode(value);
      int encodedValueLength = encoded.limit();
      ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + encodedValueLength);
      bb.putInt(encodedValueLength);
      bb.put(encoded);
      bb.flip();
      return bb;
    }

    @Override
    public ByteBuffer getRandomPacket() {
      var random = ThreadLocalRandom.current();
      int length = random.nextInt(BUFFER_SIZE) % (BUFFER_SIZE - Integer.BYTES);
      return getPacket(length);
    }

    @Override
    public ByteBuffer getBoundedRandomPacket(int length) {
      return getPacket(length);
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
      int timeout, int strLength) throws IOException {
    super(servAddr, packetBuilder, size, timeout, strLength);
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
    var arguments = Helper.DEFAULT_ARGUMENTS;
    arguments
      .put("charset",
          new AbstractMap.SimpleEntry<String, Boolean>("charset, default is UTF-8", false));

    var options = Helper.buildOptions(Helper.DEFAULT_ARGUMENTS);
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("java StringClient", options);
      return;
    }

    String host = cmd.getOptionValue("host");
    int port = Integer.valueOf(cmd.getOptionValue("port"));
    int size = parseInt(cmd.getOptionValue("numberOfElements")).orElse(10);
    int timeout = parseInt(cmd.getOptionValue("timeout")).orElse(1);
    int length = parseInt(cmd.getOptionValue("length")).orElse(Integer.MAX_VALUE);

    String charsetName = cmd.getOptionValue("charset") == null //
        ? "UTF-8"
        : cmd.getOptionValue("charset");

    Charset charset = Charset.isSupported(charsetName) //
        ? Charset.forName(charsetName)
        : StandardCharsets.UTF_8;

    InetSocketAddress server = new InetSocketAddress(host, port);
    var packetBuilder = new StringPacket(charset);

    StringClient client = new StringClient(server, packetBuilder, size, timeout, length);
    client.launch();
    client.free();
  }

}
