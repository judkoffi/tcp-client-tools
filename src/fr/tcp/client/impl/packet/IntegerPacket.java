package fr.tcp.client.impl.packet;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import fr.tcp.client.IPacket;

public class IntegerPacket implements IPacket<Integer> {
  private final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
  private final ThreadLocalRandom random = ThreadLocalRandom.current();

  @Override
  public ByteBuffer getRandomPacket() {
    buffer.clear();
    buffer.putInt(random.nextInt(Integer.MAX_VALUE));
    return buffer.flip();
  }

  @Override
  public Integer getValueFrom(ByteBuffer bb) {
    bb.flip();
    int value = bb.getInt();
    bb.compact();
    return value;
  }

  @Override
  public ByteBuffer getBoundedRandomPacket(int lenght) {
    buffer.clear();
    buffer.putInt(random.nextInt(lenght));
    return buffer.flip();
  }

  @Override
  public ByteBuffer getPacketFromValue(Integer value) {
    buffer.clear();
    buffer.putInt(value);
    return buffer.flip();
  }
}
