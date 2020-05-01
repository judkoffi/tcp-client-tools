package fr.tcp.client.impl;

import static fr.tcp.client.Helper.BUFFER_SIZE;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.Helper;
import fr.tcp.client.IClient;
import fr.tcp.client.IPacket;
import fr.tcp.client.impl.packet.StringPacket;


public class StringClient extends AbstractClient<String> {
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
    int length = parseInt(cmd.getOptionValue("length")).orElse(BUFFER_SIZE - Integer.BYTES);

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
