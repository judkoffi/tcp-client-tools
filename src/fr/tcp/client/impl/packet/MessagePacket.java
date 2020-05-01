package fr.tcp.client.impl.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import fr.tcp.client.Helper;
import fr.tcp.client.IPacket;
import fr.tcp.client.model.Message;

public class MessagePacket implements IPacket<Message> {
  private final StringPacket packet;

  public MessagePacket(Charset charset) {
    this.packet = new StringPacket(charset);
  }

  private ByteBuffer getPacket(int length) {
    ByteBuffer bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    bb.put(packet.getPacketFromValue(5));
    bb.put(packet.getBoundedRandomPacket(length));
    bb.flip();
    return bb;
  }

  @Override
  public ByteBuffer getRandomPacket() {
    return getPacket(Helper.BUFFER_SIZE);
  }

  @Override
  public ByteBuffer getBoundedRandomPacket(int length) {
    return getPacket(Helper.BUFFER_SIZE);
  }

  @Override
  public Message getValueFrom(ByteBuffer bb) throws IOException {
    // TODO Auto-generated method stub
    return new Message("toto", "toto");
  }

  @Override
  public ByteBuffer getPacketFromValue(Message value) {
    return value.toBuffer();
  }
}
