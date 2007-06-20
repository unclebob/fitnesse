// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;

import java.io.*;
import java.util.*;

public class MockResultFormatter implements ResultFormatter
{
	public List results = new LinkedList();
	public Counts finalCounts;
	public StringBuffer output = new StringBuffer("Mock Results:\n");

	public void acceptResult(PageResult result) throws Exception
	{
		results.add(result);
		output.append(result.toString());
	}

	public void acceptFinalCount(Counts count) throws Exception
	{
		finalCounts = count;
		output.append("Finals Counts: " + count.toString());
	}

	public int getByteCount()
	{
		return output.toString().getBytes().length;
	}

	public InputStream getResultStream() throws Exception
	{
		return new ByteArrayInputStream(output.toString().getBytes());
	}

}
