// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("fitnesse", new Class[]{
			fitnesse.wikitext.AllTestSuite.class,
			fitnesse.wiki.AllTestSuite.class,
			fitnesse.http.AllTestSuite.class,
			fitnesse.responders.AllTestSuite.class,
			fitnesse.components.AllTestSuite.class,
			fitnesse.socketservice.AllTestSuite.class,
			fitnesse.fixtures.AllTestSuite.class,
			fitnesse.updates.AllTestSuite.class,
			fitnesse.schedule.AllTestSuite.class,
			fitnesse.util.AllTestSuite.class,
			fitnesse.testutil.AllTestSuite.class,
			fitnesse.authentication.AllTestSuite.class,
			fitnesse.runner.AllTestSuite.class,
			FitNesseServerTest.class,
			FitNesseMainTest.class,
			ArgumentsTest.class,
			TestRunnerTest.class,
			FixtureTemplateCreatorTest.class,
			FitNesseExpediterTest.class,
			ComponentFactoryTest.class,
			ShutdownTest.class
		});
	}
}
