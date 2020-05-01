package fr.tcp.client.model;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import fr.tcp.client.Helper;

public class Message {
  private final String login;
  private final String message;
  private final ByteBuffer bb;
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  public Message(String login, String message) {
    this.login = login;
    this.message = message;
    this.bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    fillBuffer();
  }

  private void fillBuffer() {
    ByteBuffer loginBuffer = DEFAULT_CHARSET.encode(login);
    ByteBuffer messageBuffer = DEFAULT_CHARSET.encode(message);
    bb.putInt(loginBuffer.limit());
    bb.put(loginBuffer);
    bb.putInt(messageBuffer.limit());
    bb.put(messageBuffer);
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }

  public int getTotalSize() {
    return bb.limit();
  }
}
