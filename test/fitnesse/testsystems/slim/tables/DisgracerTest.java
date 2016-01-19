package fitnesse.testsystems.slim.tables;

import org.junit.Test;

import static org.junit.Assert.*;

public class DisgracerTest {

  @Test
  public void disgraceClassName() {
    assertEquals("classname", Disgracer.disgraceClassName("classname"));
    assertEquals("className", Disgracer.disgraceClassName("className"));
    assertEquals("ClassName", Disgracer.disgraceClassName("class name"));
  }

  @Test
  public void disgraceMethodName() {
    assertEquals("methodName", Disgracer.disgraceMethodName("method name"));
    assertEquals("methodname", Disgracer.disgraceMethodName("methodname"));
    assertEquals("MethodName", Disgracer.disgraceMethodName("Method name"));
  }
}