// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("html", new Class[] {
			HtmlPageTest.class,
			HtmlTagTest.class,
			HtmlUtilTest.class
		});
	}
}

