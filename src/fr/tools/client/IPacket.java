package fr.tools.client;

import java.nio.ByteBuffer;

/*********************************
 * CONTENT_SIZE | CONTENT(UTF8) *
 ********************************/
public interface IPacket<E> {

  ByteBuffer toBuffer();

  E getValue();
}
