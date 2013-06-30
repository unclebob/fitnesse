package fitnesse.testsystems.slim.tables;

import org.junit.Test;

import static fitnesse.testsystems.slim.tables.ComparatorUtil.approximatelyEqual;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComparatorUtilTest {


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
  public void classicRoundUp() throws Exception {
    assertTrue(approximatelyEqual("3.05", "3.049"));
  }

}
