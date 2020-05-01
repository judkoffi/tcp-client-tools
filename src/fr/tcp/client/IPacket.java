package fr.tcp.client;

import java.io.IOException;
import java.nio.ByteBuffer;

/*********************************
 * CONTENT_SIZE | CONTENT(UTF8) *
 ********************************/
public interface IPacket<E> {

  /**
   * Generate a random buffer
   * 
   * @return a ByteBuffer ready to be send (read mode)
   */
  ByteBuffer getRandomPacket();


  /**
   * Generate a random bounded buffer
   * 
   * @param lenght: length of buffer content
   * @return a ByteBuffer ready to be send (read mode)
   */
  ByteBuffer getBoundedRandomPacket(int length);

  /**
   * Get E value from incoming buffer a write mode
   * 
   * @param bb: origin buffer
   * @return E value
   * @throws IOException
   */
  E getValueFrom(ByteBuffer bb) throws IOException;
}
