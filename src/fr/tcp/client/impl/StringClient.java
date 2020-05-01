package fr.tcp.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import fr.tcp.client.AbstractClient;
import fr.tcp.client.IPacket;

public class StringClient extends AbstractClient<String> {

  public StringClient(InetSocketAddress servAddr, IPacket<String> packetBuilder, int size,
      int timeout) throws IOException {
    super(servAddr, packetBuilder, size, timeout);
  }

  @Override
  public void readPacket() {}

}
