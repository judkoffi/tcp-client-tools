package fr.tools.client;

import java.nio.ByteBuffer;

public interface IClient<E> {
  public IPacket<E> buildPacket(E elt);

  public IPacket<E> buildPacket(ByteBuffer buffer);

  public void sendPacket();

  public void readPacket();

  public void launch();

  public void free();
}
