// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.*;
import fitnesse.components.FitProtocol;
import fitnesse.http.*;
import fitnesse.runner.*;
import fitnesse.util.StreamReader;

import java.io.*;

public class TestResultFormattingResponder implements Responder
{
	public ResultFormatter formatter = new MockResultFormatter();
	public Counts finalCounts;
	private FitNesseContext context;
	private String baseUrl;
	private String rootPath;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		init(context, request);

		String results = (String) request.getInput("results");
		byte[] bytes = results.getBytes("UTF-8");
		processResults(new ByteArrayInputStream(bytes));

		InputStreamResponse response = new InputStreamResponse();
		response.setBody(formatter.getResultStream(), formatter.getByteCount());
//		InputStream resultsStream = formatter.getResultStream();
//		StreamReader reader = new StreamReader(resultsStream);
//		response.setContent(reader.read(formatter.getByteCount()));

		return response;
	}

	public void init(FitNesseContext context, Request request) throws Exception
	{
		this.context = context;
		baseUrl = (String) request.getHeader("Host");
		rootPath = (String) request.getResource();
		formatter = makeFormatter(request);
	}

	public void processResults(InputStream input) throws Exception
	{
		StreamReader reader = new StreamReader(input);
		boolean readingResults = true;
		while(readingResults)
		{
			int bytesToRead = FitProtocol.readSize(reader);
			if(bytesToRead != 0)
			{
				String resultString = reader.read(bytesToRead);
				PageResult result = PageResult.parse(resultString);
				formatter.acceptResult(result);
			}
			else
				readingResults = false;
		}
		formatter.acceptFinalCount(FitProtocol.readCounts(reader));
	}

	public ResultFormatter makeFormatter(Request request) throws Exception
	{
		String format = (String) request.getInput("format");
		if(format != null)
		{
			if("html".equals(format))
				return new HtmlResultFormatter(context.htmlPageFactory, baseUrl, rootPath);
			if("xml".equals(format))
				return new XmlResultFormatter(baseUrl, rootPath);
		}
		return new MockResultFormatter();
	}
}
