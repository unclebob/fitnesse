// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import junit.framework.*;
import fitnesse.testutil.TestSuiteMaker;
import fit.exception.*;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("fit", new Class[] {
			ParseTest.class,
			TypeAdapterTest.class,
			FitServerTest.class,
			FitMatcherTest.class,
			ColumnFixtureTest.class,
      RowFixtureTest.class,
			FixtureTest.class,
			ScientificDoubleTest.class,
			GracefulNamerTest.class,
			FixtureLoaderTest.class,
			FriendlyErrorTest.class,
			BindingTest.class,
			CountsTest.class
		});
  }
}
