package fitnesse.responders.run.slimResponder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static fitnesse.responders.run.slimResponder.SlimTable.Disgracer.*;

public class SlimTableTest {
  @Test
  public void gracefulClassNames() throws Exception {
    assertEquals("MyClass", disgraceClassName("my class"));
    assertEquals("myclass", disgraceClassName("myclass"));
    assertEquals("x.y", disgraceClassName("x.y"));
    assertEquals("x_y", disgraceClassName("x_y"));
    assertEquals("MeAndMrs_jones", disgraceClassName("me and mrs_jones"));
  }

  @Test
  public void gracefulMethodNames() throws Exception {
    assertEquals("myMethodName", disgraceMethodName("my method name"));
    assertEquals("myMethodName", disgraceMethodName("myMethodName"));
    assertEquals("my_method_name", disgraceMethodName("my_method_name"));
    assertEquals("getStringArgs", disgraceMethodName("getStringArgs"));
  }


}
