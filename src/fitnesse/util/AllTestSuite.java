// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("util", new Class[]{
			WildcardTest.class,
			StreamReaderTest.class,
			FileUtilTest.class,
			StringUtilTest.class,
			XmlUtilTest.class
		});
	}
}
