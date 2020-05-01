package fr.tools.client;

import java.io.IOException;
import java.nio.ByteBuffer;

/*********************************
 * CONTENT_SIZE | CONTENT(UTF8) *
 ********************************/
public interface IPacket<E> {

  ByteBuffer buildBuffer(E value);

  E getValueFrom(ByteBuffer bb) throws IOException;
}
