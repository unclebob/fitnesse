// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import junit.framework.*;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("refactoring", new Class[] {
			DeletePageResponderTest.class,
			RenamePageResponderTest.class,
			RefactorPageResponderTest.class,
			MovePageResponderTest.class
		});
	}
}
