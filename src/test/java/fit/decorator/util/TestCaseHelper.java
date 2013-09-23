package fit.decorator.util;

import static org.junit.Assert.assertEquals;

import fit.Counts;

public class TestCaseHelper {
  public static void assertCounts(Counts expected, Counts actual) {
    assertEquals(expected.wrong, actual.wrong);
    assertEquals(expected.exceptions, actual.exceptions);
    assertEquals(expected.ignores, actual.ignores);
    assertEquals(expected.right, actual.right);
  }

  public static Counts counts(int right, int wrong, int ignores, int exceptions) {
    Counts expected = new Counts();
    expected.right = right;
    expected.wrong = wrong;
    expected.ignores = ignores;
    expected.exceptions = exceptions;
    return expected;
  }
}
