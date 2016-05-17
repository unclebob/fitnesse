package fitnesse.slim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.*;

public class SlimStreamReaderTest {

  @Test
  public void encodeMessageToSend() throws IOException {
    ByteArrayOutputStream writer = new ByteArrayOutputStream();
    SlimStreamReader.sendSlimMessage(writer, "foo bar baz");
    assertEquals("000011:foo bar baz", writer.toString());
  }

  @Test
  public void encodeHeaderToSend() throws IOException {
    ByteArrayOutputStream writer = new ByteArrayOutputStream();
    SlimStreamReader.sendSlimHeader(writer, "foo bar baz");
    assertEquals("foo bar baz", writer.toString());
  }


  @Test
  public void readSlimMessage() throws IOException {
    InputStream input = new ByteArrayInputStream("000011:foo bar baz".getBytes());
    SlimStreamReader reader = new SlimStreamReader(input);

    String message = reader.getSlimMessage();

    assertEquals("foo bar baz", message);
  }

  @Test( expected = IOException.class )
  public void readIncompleteSlimMessage() throws IOException {
    InputStream input = new ByteArrayInputStream("00".getBytes());
    SlimStreamReader reader = new SlimStreamReader(input);

    reader.getSlimMessage();
  }

}