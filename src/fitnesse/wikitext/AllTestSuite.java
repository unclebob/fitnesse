// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import junit.framework.Test;
import junit.framework.TestSuite;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("wikitext", new Class[] {
			fitnesse.wikitext.widgets.AllTestSuite.class,
			WidgetBuilderTest.class,
			WikiTextTranslatorTest.class
		});
	}
}
