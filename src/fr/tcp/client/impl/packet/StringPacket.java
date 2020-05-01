package fr.tcp.client.impl.packet;

import static fr.tcp.client.Helper.BUFFER_SIZE;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;
import fr.tcp.client.Helper;
import fr.tcp.client.IPacket;

public class StringPacket implements IPacket<String> {
  private final Charset charset;

  public StringPacket(Charset charset) {
    this.charset = charset;
  }

  public ByteBuffer buildPacketFromValue(String value) {
    ByteBuffer encoded = charset.encode(value);
    int encodedValueLength = encoded.limit();
    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + encodedValueLength);
    bb.putInt(encodedValueLength);
    bb.put(encoded);
    bb.flip();
    return bb;
  }

  @Override
  public ByteBuffer getRandomPacket() {
    var random = ThreadLocalRandom.current();
    int length = random.nextInt(BUFFER_SIZE) % (BUFFER_SIZE - Integer.BYTES);
    return getBoundedRandomPacket(length);
  }

  @Override
  public ByteBuffer getBoundedRandomPacket(int length) {
    return buildPacketFromValue(Helper.generateRandomString(length));
  }

  public ByteBuffer getPacketFromValue(int length) {
    return buildPacketFromValue(Helper.generateRandomString(length));
  }

  @Override
  public String getValueFrom(ByteBuffer bb) throws IOException {
    bb.flip();
    String str = charset.decode(bb).toString();
    bb.compact();
    return str;
  }

  @Override
  public ByteBuffer getPacketFromValue(String value) {
    return buildPacketFromValue(value);
  }
}
