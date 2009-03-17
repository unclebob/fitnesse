// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;

public class RenameFileResponderTest extends TestCase {
  private MockRequest request;
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

  public void testMakeResponse() throws Exception {
    File file = new File("testdir/testfile");
    assertTrue(file.createNewFile());
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "testfile");
    request.addInput("newName", "newName");
    request.setResource("");
    Response response = responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertTrue(new File("testdir/newName").exists());
    assertEquals(303, response.getStatus());
    assertEquals("/", response.getHeader("Location"));
  }

  public void testRenameWithTrailingSpace() throws Exception {
    File file = new File("testdir/testfile");
    assertTrue(file.createNewFile());
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "testfile");
    request.addInput("newName", "new Name With Space ");
    request.setResource("");
    responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertTrue(new File("testdir/new Name With Space").exists());
  }

}
