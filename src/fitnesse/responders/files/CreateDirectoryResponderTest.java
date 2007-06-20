// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.util.FileUtil;
import junit.framework.TestCase;

import java.io.File;

public class CreateDirectoryResponderTest extends TestCase
{
	public void setUp() throws Exception
	{
		FileUtil.makeDir("testdir");
		FileUtil.makeDir("testdir/files");
	}

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory("testdir");
	}

	public void testMakeResponse() throws Exception
	{
		FitNesseContext context = new FitNesseContext();
		context.rootPagePath = "testdir";
		CreateDirectoryResponder responder = new CreateDirectoryResponder();
		MockRequest request = new MockRequest();
		request.addInput("dirname", "subdir");
		request.setResource("");

		Response response = responder.makeResponse(context, request);

		File file = new File("testdir/subdir");
		assertTrue(file.exists());
		assertTrue(file.isDirectory());

		assertEquals(303, response.getStatus());
		assertEquals("/", response.getHeader("Location"));
	}
}
