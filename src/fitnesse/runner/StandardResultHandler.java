// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import java.io.*;

import fitnesse.responders.run.TestSystemBase;

//TODO MDM Rename to VerboseResultHandler
public class StandardResultHandler implements ResultHandler
{
	private PrintStream output;
	private TestSystemBase.TestSummary pageCounts = new TestSystemBase.TestSummary();

	public StandardResultHandler(PrintStream output)
	{
		this.output = output;
	}

	public void acceptResult(PageResult result) throws Exception
	{
		TestSystemBase.TestSummary testSummary = result.testSummary();
		pageCounts.tallyPageCounts(testSummary);
		for(int i = 0; i < testSummary.right; i++)
			output.print(".");
		if(testSummary.wrong > 0 || testSummary.exceptions > 0)
		{
			output.println();
			if(testSummary.wrong > 0)
				output.println(pageDescription(result) + " has failures");
			if(testSummary.exceptions > 0)
				output.println(pageDescription(result) + " has errors");
		}
	}

	private String pageDescription(PageResult result)
	{
		String description = result.title();
		if("".equals(description))
			description = "The test";
		return description;
	}

	public void acceptFinalCount(TestSystemBase.TestSummary testSummary) throws Exception
	{
		output.println();
		output.println("Test Pages: " + pageCounts);
		output.println("Assertions: " + testSummary);
	}

	public int getByteCount()
	{
		return 0;
	}

	public InputStream getResultStream() throws Exception
	{
		return null;
	}
}
