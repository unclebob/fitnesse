// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;

import java.io.*;

//TODO MDM Rename to VerboseResultHandler
public class StandardResultHandler implements ResultHandler
{
	private PrintStream output;
	private Counts pageCounts = new Counts();

	public StandardResultHandler(PrintStream output)
	{
		this.output = output;
	}

	public void acceptResult(PageResult result) throws Exception
	{
		Counts counts = result.counts();
		pageCounts.tallyPageCounts(counts);
		for(int i = 0; i < counts.right; i++)
			output.print(".");
		if(counts.wrong > 0 || counts.exceptions > 0)
		{
			output.println();
			if(counts.wrong > 0)
				output.println(pageDescription(result) + " has failures");
			if(counts.exceptions > 0)
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

	public void acceptFinalCount(Counts count) throws Exception
	{
		output.println();
		output.println("Test Pages: " + pageCounts);
		output.println("Assertions: " + count);
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
