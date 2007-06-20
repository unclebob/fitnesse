// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("responders", new Class[]{
			fitnesse.html.AllTestSuite.class,
			fitnesse.responders.files.AllTestSuite.class,
			fitnesse.responders.refactoring.AllTestSuite.class,
			fitnesse.responders.editing.AllTestSuite.class,
			fitnesse.responders.run.AllTestSuite.class,
			fitnesse.responders.search.AllTestSuite.class,
			fitnesse.responders.versions.AllTestSuite.class,
			WikiPageResponderTest.class,
			ResponderFactoryTest.class,
			ErrorResponderTest.class,
			NotFoundResponderTest.class,
			SerializedPageResponderTest.class,
			NameResponderTest.class,
			PageDataResponderTest.class,
			RawContentResponderTest.class,
			RssResponderTest.class,
			WikiImportingResponderTest.class,
			ShutdownResponderTest.class,
			ChunkingResponderTest.class,
			WikiImportPropertyTest.class,
			WikiImporterTest.class,
			ImportAndViewResponderTest.class,
			WikiImportTestEventListenerTest.class
		});
	}
}
