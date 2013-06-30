package fitnesse.testsystems;

import org.junit.Test;

import static fitnesse.testsystems.ClientBuilder.replace;
import static org.junit.Assert.assertEquals;

public class ClientBuilderTest {

  @Test
  public void shouldReplaceMarkWithValue() {
    assertEquals("Hello world", replace("Hello %p", "%p", "world"));
    assertEquals("/path/to/somewhere", replace("/path/%p/somewhere", "%p", "to"));
    assertEquals("/path/to/somewhere", replace("/path%p", "%p", "/to/somewhere"));
    assertEquals("\\path\\to\\somewhere", replace("\\path\\%p\\somewhere", "%p", "to"));
    assertEquals("\\path\\to\\somewhere", replace("\\path%p", "%p", "\\to\\somewhere"));
  }

}
