package fr.tools.client;

public interface IClient<E> {
  public IPacket<E> buildPacket(E elt);

  public void sendPacket();

  public void readPacket();

  public void launch();

  public void free();
}
