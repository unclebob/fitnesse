// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import junit.framework.*;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("editing", new Class[] {
			EditResponderTest.class,
			SaveResponderTest.class,
			MergeResponderTest.class,
			PropertiesResponderTest.class,
			SavePropertiesResponderTest.class,
			TableWizardResponderTest.class,
			SymbolicLinkResponderTest.class
		});
	}
}
