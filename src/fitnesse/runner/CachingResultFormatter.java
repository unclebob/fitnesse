package fitnesse.runner;

import fit.Counts;
import java.io.*;
import java.util.*;
import fitnesse.components.*;

public class CachingResultFormatter implements ResultFormatter
{
	private ContentBuffer buffer;
	public List subHandlers = new LinkedList();

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

	public void acceptFinalCount(Counts count) throws Exception
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FitProtocol.writeCounts(count, output);
		buffer.append(output.toByteArray());

		for(Iterator iterator = subHandlers.iterator(); iterator.hasNext();)
			((ResultHandler) iterator.next()).acceptFinalCount(count);
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
