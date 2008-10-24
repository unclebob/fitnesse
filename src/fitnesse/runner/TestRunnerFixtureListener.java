// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.*;
import fitnesse.responders.run.TestSummary;

public class TestRunnerFixtureListener implements FixtureListener
{
	public Counts counts = new Counts();
	private boolean atStartOfResult = true;
	private PageResult currentPageResult;
	private TestRunner runner;

	public TestRunnerFixtureListener(TestRunner runner)
	{
		this.runner = runner;
	}

	public void tableFinished(Parse table)
	{
		try
		{
			String data = new String(FitServer.readTable(table), "UTF-8");
			if(atStartOfResult)
			{
				int indexOfFirstLineBreak = data.indexOf("\n");
				String pageTitle = data.substring(0, indexOfFirstLineBreak);
				data = data.substring(indexOfFirstLineBreak + 1);
				currentPageResult = new PageResult(pageTitle);
				atStartOfResult = false;
			}
			currentPageResult.append(data);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void tablesFinished(Counts counts)
	{
		try
		{
      TestSummary testSummary = new TestSummary();
      testSummary.right = counts.right;
      testSummary.wrong = counts.wrong;
      testSummary.ignores = counts.ignores;
      testSummary.exceptions = counts.exceptions;
      currentPageResult.setTestSummary(testSummary);
			runner.acceptResults(currentPageResult);
			atStartOfResult = true;
			this.counts.tally(counts);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
