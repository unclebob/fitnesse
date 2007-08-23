// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
  public static Test suite()
  {
	  return TestSuiteMaker.makeSuite("root", new Class[] {
			fitnesse.AllTestSuite.class,
			fit.AllTestSuite.class,
            fit.decorator.AllTestSuite.class
    });
  }
}
