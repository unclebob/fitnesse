package fitnesse.slim.test;

import fitnesse.testsystems.slim.CustomComparator;

public class InverseComparator implements CustomComparator {

  @Override
  public boolean matches(String actual, String expected) {
    return !actual.equals(expected);
  }

}
