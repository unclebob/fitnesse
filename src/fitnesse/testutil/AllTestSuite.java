// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import junit.framework.*;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("testutil", new Class[] {
			CartesianVectorTest.class
		});
	}
}
