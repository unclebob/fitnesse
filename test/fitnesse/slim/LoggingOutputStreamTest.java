package fitnesse.slim;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class LoggingOutputStreamTest {
  @Test
  public void handlesSingleLine() throws IOException {
    assertEquals(
      "ABC.:Hello World\n",
      printlnOutput("Hello World"));
  }

  @Test
  public void handlesMultiLine() throws IOException {
    assertEquals(
      "ABC.:Hello World\n" +
        "ABC :\n" +
        "ABC :The weather is nice!\n",
      printlnOutput("Hello World\n\nThe weather is nice!"));
  }

  @Test
  public void handlesMultiLineWithNewlineEnd() throws IOException {
    assertEquals(
      "ABC.:Hello World\n" +
        "ABC :The weather is nice!\n" +
        "ABC :\n",
      printlnOutput("Hello World\nThe weather is nice!\n"));
  }

  @Test
  public void handlesMultiprintln() throws IOException {
    assertEquals(
      "ABC.:Hello World\n" +
        "ABC.:The weather is nice!\n",
      outputWith(s -> {
        s.println("Hello World");
        s.println("The weather is nice!");
      }));
  }

  @Test
  public void handlesMultiprint() throws IOException {
    assertEquals(
      "ABC.:Hello\n" +
      "ABC.: \n" +
      "ABC.:World\n",
      outputWith(s -> {
        s.print("Hello");
        s.print(" ");
        s.print("World");
        s.print("\n");
      }));
  }

  @Test
  public void handlesExtraFlushes() throws IOException {
    assertEquals(
      "ABC.:Hello\n" +
        "ABC.: \n" +
        "ABC.:World\n" +
        "ABC.:The weather is nice!\n",
      outputWith(s -> {
        s.print("Hello");
        s.flush();
        s.flush();
        s.print(" ");
        s.print("World");
        s.println();
        s.flush();
        s.println("The weather is nice!");
        s.flush();
      }));
  }

  private String outputWith(Consumer<PrintStream> consumer) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream myStream = new PrintStream(baos)) {
      try (PrintStream stream = createStream(myStream)) {
        consumer.accept(stream);
      }
      return baos.toString();
    }
  }

  private String printlnOutput(String input) throws IOException {
    return outputWith(s -> s.println(input));
  }

  private PrintStream createStream(PrintStream originalStream) throws UnsupportedEncodingException {
    return SlimPipeSocket.wrapStream(originalStream, "ABC");
  }
}
