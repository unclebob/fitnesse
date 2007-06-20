// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fitnesse.fixtures;

import fit.CannotLoadFixtureTest;
import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("fixtures", new Class[]
			{RowEntryFixtureTest.class,
				PageCreatorTest.class,
				HandleFixtureDoesNotExtendFixtureTest.class,
				CannotLoadFixtureTest.class});
	}
}