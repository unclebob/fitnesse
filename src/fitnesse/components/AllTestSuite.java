// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import junit.framework.*;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("components", new Class[] {
			SaveRecorderTest.class,
			RecentChangesTest.class,
			SearcherTest.class,
			CommandRunnerTest.class,
			LoggerTest.class,
			ClassPathBuilderTest.class,
			WhereUsedTest.class,
			PageReferenceRenamerTest.class,
			FitClientTest.class,
			Base64Test.class,
			CommandLineTest.class,
			ContentBufferTest.class,
			XmlWriterTest.class
		});
	}
}
