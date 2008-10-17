// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fitnesse.components.*;
import fitnesse.responders.run.TestSystem;

import java.io.*;
import java.util.*;

import fit.Counts;

public class CachingResultFormatter implements ResultFormatter
{
	private ContentBuffer buffer;
	public List<ResultHandler> subHandlers = new LinkedList<ResultHandler>();

	public CachingResultFormatter() throws Exception
	{
		buffer = new ContentBuffer(".results");
	}

	public void acceptResult(PageResult result) throws Exception
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FitProtocol.writeData(result.toString() + "\n", output);
		buffer.append(output.toByteArray());

		for(Iterator iterator = subHandlers.iterator(); iterator.hasNext();)
			((ResultHandler) iterator.next()).acceptResult(result);
	}

	public void acceptFinalCount(TestSystem.TestSummary testSummary) throws Exception
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
    Counts counts = new Counts(testSummary.right, testSummary.wrong, testSummary.ignores, testSummary.exceptions);
    FitProtocol.writeCounts(counts, output);
		buffer.append(output.toByteArray());

		for(Iterator iterator = subHandlers.iterator(); iterator.hasNext();)
			((ResultHandler) iterator.next()).acceptFinalCount(testSummary);
	}

	public int getByteCount() throws Exception
	{
		return buffer.getSize();
	}

	public InputStream getResultStream() throws Exception
	{
		return buffer.getNonDeleteingInputStream();
	}

	public void cleanUp() throws Exception
	{
		buffer.delete();
	}

	public void addHandler(ResultHandler handler)
	{
		subHandlers.add(handler);
	}
}
