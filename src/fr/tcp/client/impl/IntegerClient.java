package fr.tcp.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.Helper;
import fr.tcp.client.IClient;
import fr.tcp.client.IPacket;
import fr.tcp.client.impl.packet.IntegerPacket;

public class IntegerClient extends AbstractClient<Integer> {



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
    var options = Helper.buildOptions(Helper.DEFAULT_ARGUMENTS);
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("java IntegerClient", options);
      return;
    }

    String host = cmd.getOptionValue("host");
    int port = Integer.valueOf(cmd.getOptionValue("port"));
    int size = parseInt(cmd.getOptionValue("numberOfElements")).orElse(10);
    int timeout = parseInt(cmd.getOptionValue("timeout")).orElse(1);
    int length = parseInt(cmd.getOptionValue("length")).orElse(Integer.MAX_VALUE);

    InetSocketAddress server = new InetSocketAddress(host, port);
    var packetBuilder = new IntegerPacket();

    IntegerClient client = new IntegerClient(server, packetBuilder, size, timeout, length);
    client.launch();
    client.free();
  }
}
