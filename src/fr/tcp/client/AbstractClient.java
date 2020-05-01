package fr.tcp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public abstract class AbstractClient<E> implements IClient<E> {

  private final Thread reader;
  private final Thread write;
  protected SocketChannel channel;
  protected final int size;
  protected final int timeout;
  protected final IPacket<E> packetBuilder;

  public AbstractClient(InetSocketAddress servAddr, IPacket<E> packetBuilder, int size, int timeout)
      throws IOException {
    this.reader = new Thread(this::readPacket);
    this.write = new Thread(this::sendPacket);
    this.size = size;
    this.timeout = timeout;
    this.packetBuilder = packetBuilder;
    this.channel = SocketChannel.open(servAddr);
  }

  @Override
  public void sendPacket() {
    for (var i = 0; i < size; i++) {
      try {
        channel.write(packetBuilder.getRandomPacket());
        Thread.sleep(timeout);
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
  public void free() throws IOException, InterruptedException {
    reader.join();
    write.join();
    channel.close();
  }
}
