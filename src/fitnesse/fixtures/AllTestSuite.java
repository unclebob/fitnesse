// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fitnesse.fixtures;

import junit.framework.*;
import fit.CannotLoadFixtureTest;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
  public static Test suite()
  {
    return TestSuiteMaker.makeSuite("fixtures", new Class[]
    { RowEntryFixtureTest.class,
      PageCreatorTest.class,
      HandleFixtureDoesNotExtendFixtureTest.class,
      CannotLoadFixtureTest.class });
  }
}