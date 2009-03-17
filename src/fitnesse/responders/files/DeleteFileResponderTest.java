// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import util.FileUtil;
import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;

public class DeleteFileResponderTest extends RegexTestCase {
  public MockRequest request;
  private FitNesseContext context;

  public void setUp() {
    FileUtil.makeDir("testdir");
    request = new MockRequest();
    context = new FitNesseContext();
    context.rootPagePath = "testdir";
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testdir");
  }

  public void testDelete() throws Exception {
    File file = new File("testdir/testfile");
    assertTrue(file.createNewFile());
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "testfile");
    request.setResource("");
    Response response = responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertEquals(303, response.getStatus());
    assertEquals("/", response.getHeader("Location"));
  }

  public void testDeleteDirectory() throws Exception {
    File dir = new File("testdir/dir");
    assertTrue(dir.mkdir());
    File file = new File(dir, "testChildFile");
    assertTrue(file.createNewFile());
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "dir");
    request.setResource("");
    responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertFalse(dir.exists());

  }
}
