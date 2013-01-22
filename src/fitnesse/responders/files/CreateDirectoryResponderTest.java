// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;

public class CreateDirectoryResponderTest extends TestCase {
  private FitNesseContext context;

  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext(null);
    FileUtil.makeDir(context.getRootPagePath());
    FileUtil.makeDir(context.getRootPagePath() + "/files");
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(context.getRootPagePath());
  }

  public void testMakeResponse() throws Exception {
    CreateDirectoryResponder responder = new CreateDirectoryResponder();
    MockRequest request = new MockRequest();
    request.addInput("dirname", "subdir");
    request.setResource("");

    Response response = responder.makeResponse(context, request);

    File file = new File(context.getRootPagePath() + "/subdir");
    assertTrue(file.exists());
    assertTrue(file.isDirectory());

    assertEquals(303, response.getStatus());
    assertEquals("/", response.getHeader("Location"));
  }
}
