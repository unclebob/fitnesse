package fit.decorator.util;

import junit.framework.TestCase;
import fit.Counts;

public class TestCaseHelper {
  public static void assertCounts(Counts expected, Counts actual) {
    TestCase.assertEquals(expected.wrong, actual.wrong);
    TestCase.assertEquals(expected.exceptions, actual.exceptions);
    TestCase.assertEquals(expected.ignores, actual.ignores);
    TestCase.assertEquals(expected.right, actual.right);
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
