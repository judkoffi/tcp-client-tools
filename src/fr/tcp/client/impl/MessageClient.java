package fr.tcp.client.impl;

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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.Helper;
import fr.tcp.client.IClient;
import fr.tcp.client.IPacket;
import fr.tcp.client.impl.packet.MessagePacket;
import fr.tcp.client.model.Message;

public class MessageClient extends AbstractClient<Message> {
  private final String login;
  private final ByteBuffer buffer = ByteBuffer.allocate(Helper.BUFFER_SIZE);

  public MessageClient(InetSocketAddress servAddr, IPacket<Message> packetBuilder, int size,
      int timeout, int contentMaxLength, String login) throws IOException {
    super(servAddr, packetBuilder, size, timeout, contentMaxLength);
    this.login = login;
  }

  private void processMessage() throws IOException {
    var sizeBuff = ByteBuffer.allocate(Integer.BYTES);
    var oldLimit = buffer.limit();
    buffer.clear();
    IClient.readFully(channel, sizeBuff);
    int size = sizeBuff.flip().getInt();
    buffer.limit(size);
    IClient.readFully(channel, buffer);

    System.out.print(Helper.DEFAULT_CHARSET.decode(buffer.flip()));

    buffer.compact();
    buffer.limit(oldLimit);

    sizeBuff.clear();
    IClient.readFully(channel, sizeBuff);
    size = sizeBuff.flip().getInt();
    buffer.limit(size);
    IClient.readFully(channel, buffer);
    System.out.println(": " + Helper.DEFAULT_CHARSET.decode(buffer.flip()));
  }

  @Override
  public void readPacket() {
    for (var i = 0; i < size; i++) {
      try {
        processMessage();
        Thread.sleep(timeout * 2);
      } catch (IOException e) {
        System.out.println(e);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  @Override
  public void sendPacket() {
    for (var i = 0; i < size; i++) {
      try {
        channel
          .write(packetBuilder
            .getPacketFromValue(new Message(login, Helper.generateRandomString(contentMaxLength))));
        Thread.sleep(timeout);
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


    var option = new Option("name", true, "pseudo");
    option.setRequired(true);

    var options = Helper.buildOptions(Helper.DEFAULT_ARGUMENTS);
    options.addOption(option);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("java MessageClient", options);
      return;
    }

    String host = cmd.getOptionValue("host");
    int port = Integer.valueOf(cmd.getOptionValue("port"));
    int size = parseInt(cmd.getOptionValue("numberOfElements")).orElse(10);
    int timeout = parseInt(cmd.getOptionValue("timeout")).orElse(1);
    int length = parseInt(cmd.getOptionValue("length")).orElse(100);

    String login = cmd.getOptionValue("name");

    String charsetName = cmd.getOptionValue("charset") == null //
        ? "UTF-8"
        : cmd.getOptionValue("charset");

    Charset charset = Charset.isSupported(charsetName) //
        ? Charset.forName(charsetName)
        : StandardCharsets.UTF_8;

    InetSocketAddress server = new InetSocketAddress(host, port);
    MessagePacket packetBuilder = new MessagePacket(charset);

    MessageClient client = new MessageClient(server, packetBuilder, size, timeout, length, login);
    client.launch();
    client.free();
  }


}
