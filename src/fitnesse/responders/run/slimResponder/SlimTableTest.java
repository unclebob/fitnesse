package fitnesse.responders.run.slimResponder;

import static fitnesse.responders.run.slimResponder.SlimTable.Disgracer.disgraceClassName;
import static fitnesse.responders.run.slimResponder.SlimTable.Disgracer.disgraceMethodName;
import static fitnesse.responders.run.slimResponder.SlimTable.approximatelyEqual;
import static org.junit.Assert.*;
import org.junit.Test;

public class SlimTableTest {
  @Test
  public void gracefulClassNames() throws Exception {
    assertEquals("MyClass", disgraceClassName("my class"));
    assertEquals("myclass", disgraceClassName("myclass"));
    assertEquals("x.y", disgraceClassName("x.y"));
    assertEquals("x_y", disgraceClassName("x_y"));
    assertEquals("MeAndMrs_jones", disgraceClassName("me and mrs_jones"));
    assertEquals("PageCreator", disgraceClassName("Page creator."));
  }

  @Test
  public void gracefulMethodNames() throws Exception {
    assertEquals("myMethodName", disgraceMethodName("my method name"));
    assertEquals("myMethodName", disgraceMethodName("myMethodName"));
    assertEquals("my_method_name", disgraceMethodName("my_method_name"));
    assertEquals("getStringArgs", disgraceMethodName("getStringArgs"));
    assertEquals("setMyVariableName", disgraceMethodName("set myVariableName"));
  }

  @Test
  public void trulyEqual() throws Exception {
    assertTrue(approximatelyEqual("3.0", "3.0"));
  }

  @Test
  public void veryUnequal() throws Exception {
    assertFalse(approximatelyEqual("5", "3"));
  }

  @Test
  public void isWithinPrecision() throws Exception {
    assertTrue(approximatelyEqual("3", "2.5"));
  }

  @Test
  public void justTooBig() throws Exception {
    assertFalse(approximatelyEqual("3.000", "3.0005"));
  }

  @Test
  public void justTooSmall() throws Exception {
    assertFalse(approximatelyEqual("3.0000", "2.999949"));
  }

  @Test
  public void justSmallEnough() throws Exception {
    assertTrue(approximatelyEqual("-3.00", "-2.995"));
  }

  @Test
  public void justBigEnough() throws Exception {
    assertTrue(approximatelyEqual("-3.000000", "-3.000000499"));
  }

  @Test
  public void x() throws Exception {
    assertTrue(approximatelyEqual("3.05", "3.049"));
  }


}
