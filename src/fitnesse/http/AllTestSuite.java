// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import junit.framework.Test;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("http", new Class[] {
			SimpleResponseTest.class,
			RequestTest.class,
			RequestBuilderTest.class,
			ResponseParserTest.class,
			ChunkedResponseTest.class,
			InputStreamResponseTest.class
		});
	}
}
