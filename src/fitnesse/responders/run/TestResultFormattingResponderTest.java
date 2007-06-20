// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.FitNesseContext;
import fitnesse.components.FitProtocol;
import fitnesse.http.*;
import fitnesse.runner.*;
import fitnesse.testutil.RegexTest;

import java.io.*;

public class TestResultFormattingResponderTest extends RegexTest
{
	private PipedOutputStream output;
	private PipedInputStream input;
	private TestResultFormattingResponder responder;
	private MockResultFormatter formatter;
	private PageResult result1;
	private PageResult result2;
	private FitNesseContext context;

	public void setUp() throws Exception
	{
		output = new PipedOutputStream();
		input = new PipedInputStream(output);

		responder = new TestResultFormattingResponder();
		formatter = new MockResultFormatter();
		responder.formatter = formatter;

		result1 = new PageResult("Result1Title", new Counts(1, 2, 3, 4), "result1 data");
		result2 = new PageResult("Result2Title", new Counts(4, 3, 2, 1), "result2 data");

		context = new FitNesseContext();
	}

	public void testOneResult() throws Exception
	{
		FitProtocol.writeData(result1.toString(), output);
		FitProtocol.writeCounts(new Counts(0, 0, 0, 0), output);
		responder.processResults(input);

		assertEquals(1, formatter.results.size());
		assertEquals(result1.toString(), formatter.results.get(0).toString());
	}

	public void testTwoResults() throws Exception
	{
		FitProtocol.writeData(result1.toString(), output);
		FitProtocol.writeData(result2.toString(), output);
		FitProtocol.writeCounts(new Counts(0, 0, 0, 0), output);
		responder.processResults(input);

		assertEquals(2, formatter.results.size());
		assertEquals(result1.toString(), formatter.results.get(0).toString());
		assertEquals(result2.toString(), formatter.results.get(1).toString());
	}

	public void testFinalCounts() throws Exception
	{
		FitProtocol.writeData(result1.toString(), output);
		Counts counts = new Counts(1, 2, 3, 4);
		FitProtocol.writeCounts(counts, output);
		responder.processResults(input);

		assertEquals(counts, formatter.finalCounts);
	}

	public void testMakeResponse() throws Exception
	{
		MockRequest request = new MockRequest();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FitProtocol.writeData(result1.toString(), output);
		FitProtocol.writeData(result2.toString(), output);
		FitProtocol.writeCounts(new Counts(5, 5, 5, 5), output);
		request.addInput("results", output.toString());

		Response response = responder.makeResponse(context, request);
		String content = new MockResponseSender(response).sentData();

		assertSubString("Mock Results", content);
	}

	public void testMockFormatter() throws Exception
	{
		checkFormtterCreated(null, MockResultFormatter.class);
	}

	public void testHtmlFormatter() throws Exception
	{
		checkFormtterCreated("html", HtmlResultFormatter.class);
	}

	public void testXmlFormatter() throws Exception
	{
		checkFormtterCreated("xml", XmlResultFormatter.class);
	}

	private void checkFormtterCreated(String format, Class formatterClass) throws Exception
	{
		MockRequest request = new MockRequest();
		request.addHeader("Host", "locahost:8080");
		request.setResource("/");
		if(format != null)
			request.addInput("format", format);
		responder.init(context, request);
		assertEquals(formatterClass, responder.formatter.getClass());
	}
}
