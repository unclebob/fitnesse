// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;

public class RenameFileConfirmationResponderTest extends RegexTest
{
	MockRequest request;
	private FitNesseContext context;
	private String content;
	private SimpleResponse response;
	private Responder responder;

	public void setUp() throws Exception
	{
		request = new MockRequest();
		context = new FitNesseContext();
		context.rootPagePath = SampleFileUtility.base;
		SampleFileUtility.makeSampleFiles();
	}

	public void testContentOfPage() throws Exception
	{
		getContentForSimpleRename();

		assertSubString("renameFile", content);
		assertSubString("Rename File", content);
		assertSubString("Rename <b>MyFile.txt</b>", content);
	}

	public void testExistingFilenameIsInTextField() throws Exception
	{
		getContentForSimpleRename();

		assertSubString("<input type=\"text\" name=\"newName\" value=\"MyFile.txt\"/>", content);
	}

	private void getContentForSimpleRename() throws Exception
	{
		request.setResource("files");
		request.addInput("filename", "MyFile.txt");
		responder = new RenameFileConfirmationResponder();
		response = (SimpleResponse) responder.makeResponse(context, request);
		content = response.getContent();
	}

	public void testFitnesseLook() throws Exception
	{
		Responder responder = new RenameFileConfirmationResponder();
		SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
		String content = response.getContent();
		assertSubString("<link rel=\"stylesheet\" type=\"text/css\" href=\"/files/css/fitnesse.css\" media=\"screen\"/>", content);
	}

}
