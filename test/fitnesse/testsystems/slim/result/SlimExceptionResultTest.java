package fitnesse.testsystems.slim.result;

import fitnesse.testsystems.slim.results.SlimExceptionResult;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlimExceptionResultTest {
  @Test
  public void otherExceptionRecognized() {
    SlimExceptionResult result = new SlimExceptionResult("key", "my normal exception");
    assertFalse(result.hasMessage());
    assertNull(result.getMessage());
  }

  @Test
  public void messageFound() {
    SlimExceptionResult result = new SlimExceptionResult("key", "message:<<Bad things>>");
    assertTrue(result.hasMessage());
    assertEquals("Bad things", result.getMessage());
  }

  @Test
  public void messageWithNewlineFound() {
    SlimExceptionResult result = new SlimExceptionResult("key", "message:<<Bad things\nhappened>>");
    assertTrue(result.hasMessage());
    assertEquals("Bad things\nhappened", result.getMessage());
  }
}
