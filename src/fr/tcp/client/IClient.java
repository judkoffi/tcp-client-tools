package fr.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface IClient<E> {
  public void sendPacket();

  public void readPacket();

  public void launch();

  public void free() throws IOException, InterruptedException;

  public static boolean readFully(SocketChannel sc, ByteBuffer bb) throws IOException {
    while (bb.hasRemaining()) {
      int read = sc.read(bb);
      if (read == -1)
        return false;
    }
    return true;
  }

}
