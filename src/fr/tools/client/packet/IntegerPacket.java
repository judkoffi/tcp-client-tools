package fr.tools.client.packet;

import java.nio.ByteBuffer;
import fr.tools.client.IPacket;

public class IntegerPacket implements IPacket<Integer> {
  private ByteBuffer buffer;

  private IntegerPacket(int value) {
    this.buffer = ByteBuffer.allocate(Integer.BYTES);
    this.buffer.putInt(value);
  }

  private IntegerPacket(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public static IntegerPacket from(Integer value) {
    return new IntegerPacket(value);
  }

  public static IntegerPacket from(ByteBuffer buffer) {
    return new IntegerPacket(buffer);
  }

  @Override
  public ByteBuffer toBuffer() {
    return buffer.duplicate();
  }

  @Override
  public Integer getValue() {
    buffer.flip();
    int value = buffer.getInt();
    buffer.clear();
    return value;
  }
}
