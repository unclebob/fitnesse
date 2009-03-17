// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteMaker {
  public static Test makeSuite(String name, Class<?>[] tests) {
    TestSuite suite = new TestSuite(name);
    for (int i = 0; i < tests.length; i++) {
      Class<?> test = tests[i];
      try {
        if (isSuite(test))
          suite.addTest(getSuite(test));
        else
          suite.addTest(new TestSuite(test));
      }
      catch (Exception e) {
        System.err.println("Problem adding test to suite: " + test.getName());
      }
    }
    return suite;
  }

  public static boolean isSuite(Class<?> test) {
    Method suite = getSuiteMethod(test);
    return suite != null;
  }

  private static Method getSuiteMethod(Class<?> test) {
    Method suite = null;
    try {
      suite = test.getDeclaredMethod("suite", new Class<?>[]{});
    }
    catch (NoSuchMethodException e) {
    }
    return suite;
  }

  public static Test getSuite(Class<?> test) throws Exception {
    Method suite = getSuiteMethod(test);
    return (Test) suite.invoke(null, new Object[]{});
  }
}
