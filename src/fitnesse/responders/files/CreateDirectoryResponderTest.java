// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;

public class CreateDirectoryResponderTest extends TestCase {
  public void setUp() throws Exception {
    FileUtil.makeDir("testdir");
    FileUtil.makeDir("testdir/files");
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testdir");
  }

  public void testMakeResponse() throws Exception {
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
