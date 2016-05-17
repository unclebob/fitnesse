package fitnesse.slim;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import util.FileUtil;
import util.StreamReader;

public class SlimStreamReader extends StreamReader {

  public SlimStreamReader(InputStream input) {
    super(input);
  }

  public static void sendSlimMessage(OutputStream writer, String message) throws IOException {
    byte[] msgChars = message.getBytes(FileUtil.CHARENCODING);
    byte[] msgLength = String.format(SlimVersion.LENGTH_FORMAT, msgChars.length).getBytes(FileUtil.CHARENCODING);
    writer.write(msgLength, 0, msgLength.length);
    writer.write(msgChars, 0, msgChars.length);
    writer.flush();
  }

  public static void sendSlimHeader(OutputStream writer, String header) throws IOException {
    // The Header has no length information as prefix
    byte[] msgChars = header.getBytes(FileUtil.CHARENCODING);
    writer.write(msgChars, 0, msgChars.length);
    writer.flush();
  }

  private int getLengthToRead() throws IOException {
    String length = read(SlimVersion.MINIMUM_NUMBER_LENGTH);

    //Continue to read up to the ":"
    String next;
    while (!":".equals(next = read(1)) && !isEof() && !isTimeout())
      length = length + next;

    if (isEof())
      throw new IOException("Stream Read Failure. Can't read length of message, EOF reached.  Possibly test aborted.  Last things read: " + length);
    if (isTimeout())
      throw new IOException("Stream Read Failure. Can't read length of message, Timeout reached.  Possibly test aborted.  Last things read: " + length);

    try {
      Integer resultLength = Integer.parseInt(length);
      return resultLength;
    } catch (NumberFormatException e) {
      throw new IOException("Stream Read Failure. Can't read length of message, not a number.  Possibly test aborted.  Last things read: " + length);
    }
  }

  public String getSlimMessage() throws IOException {
    int resultLength = getLengthToRead();
    return read(resultLength);
  }

  public static SlimStreamReader getReader(Socket socket) throws IOException {
    return new SlimStreamReader(new BufferedInputStream(socket.getInputStream()));
  }

  public static OutputStream getByteWriter(Socket socket) throws IOException {
    return new BufferedOutputStream(socket.getOutputStream());
  }

}
