package fitnesse.runner;

import fit.Counts;
import java.util.*;
import java.io.*;

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
