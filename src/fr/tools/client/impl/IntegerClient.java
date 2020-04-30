package fr.tools.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import fr.tools.client.IClient;
import fr.tools.client.IPacket;
import fr.tools.client.packet.IntegerPacket;

public class IntegerClient implements IClient<Integer> {
  private final Thread reader;
  private final Thread write;
  private final static int SLEEP = 1;
  private SocketChannel channel;

  public IntegerClient(InetSocketAddress servAddr) throws IOException {
    this.reader = new Thread(this::readPacket);
    this.write = new Thread(this::sendPacket);
    this.channel = SocketChannel.open(servAddr);
  }

  @Override
  public IPacket<Integer> buildPacket(Integer value) {
    return IntegerPacket.from(value);
  }

  @Override
  public void sendPacket() {
    while (!Thread.interrupted()) {
      try {
        try (var input = new Scanner(System.in)) {
          while (input.hasNextLine()) {
            int value = input.nextInt();
            try {
              channel.write(buildPacket(value).toBuffer().flip());
            } catch (IOException e) {
              System.out.println(e);
            }
          }
        }
        Thread.sleep(SLEEP / 2);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  @Override
  public void readPacket() {
    while (!Thread.interrupted()) {
      try {
        try {
          var buffer = ByteBuffer.allocate(Integer.BYTES);
          channel.read(buffer);
          var packet = IntegerPacket.from(buffer);
          System.out.println("value: " + packet.getValue());
        } catch (IOException e) {
          System.out.println(e);
        }
        Thread.sleep(SLEEP);
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
    if (args.length != 2) {
      System.err.println("Usage: java IntegerClient addr port");
      return;
    }

    InetSocketAddress server = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    var client = new IntegerClient(server);
    client.launch();
    // client.free();
  }
}
