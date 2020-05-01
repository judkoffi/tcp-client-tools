package fr.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;

/*********************************
 * CONTENT_SIZE | CONTENT(UTF8) *
 ********************************/
public interface IPacket<E> {

  /**
   * Generate a random buffer to be send in read mode
   * 
   * @return a ByteBuffer ready to be send
   */
  ByteBuffer getRandomPacket();

  /**
   * Get E value from incoming buffer a write mode
   * 
   * @param bb: origin buffer
   * @return E value
   * @throws IOException
   */
  E getValueFrom(ByteBuffer bb) throws IOException;
}
