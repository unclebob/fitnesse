// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import junit.framework.Test;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("runner", new Class[] {
			TestRunnerTest.class,
			StandardResultHandlerTest.class,
			PageResultTest.class,
			HtmlResultFormatterTest.class,
			CachingResultFormatterTest.class,
			FormattingOptionTest.class,
			XmlResultFormatterTest.class
		});
	}
}

