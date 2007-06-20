// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import fitnesse.testutil.RegexTest;

import java.io.*;

public class StandardResultHandlerTest extends RegexTest
{
	private StandardResultHandler handler;
	private ByteArrayOutputStream bytes;

	public void setUp() throws Exception
	{
		bytes = new ByteArrayOutputStream();
		handler = new StandardResultHandler(new PrintStream(bytes));
	}

	public void testHandleResultPassing() throws Exception
	{
		String output = getOutputForResultWithCount(new Counts(5, 0, 0, 0));
		assertSubString(".....", output);
	}

	public void testHandleResultFailing() throws Exception
	{
		String output = getOutputForResultWithCount(new Counts(0, 1, 0, 0));
		assertSubString("SomePage has failures", output);
	}

	public void testHandleResultWithErrors() throws Exception
	{
		String output = getOutputForResultWithCount(new Counts(0, 0, 0, 1));
		assertSubString("SomePage has errors", output);
	}

	public void testHandleErrorWithBlankTitle() throws Exception
	{
		String output = getOutputForResultWithCount("", new Counts(0, 0, 0, 1));
		assertSubString("The test has errors", output);
	}

	public void testFinalCount() throws Exception
	{
		Counts counts = new Counts(5, 4, 3, 2);
		handler.acceptFinalCount(counts);

		assertSubString(counts.toString(), bytes.toString());
	}

	private String getOutputForResultWithCount(Counts counts) throws Exception
	{
		return getOutputForResultWithCount("SomePage", counts);
	}

	private String getOutputForResultWithCount(String title, Counts counts) throws Exception
	{
		PageResult result = new PageResult(title);
		result.setCounts(counts);
		handler.acceptResult(result);
		String output = bytes.toString();
		return output;
	}

}
