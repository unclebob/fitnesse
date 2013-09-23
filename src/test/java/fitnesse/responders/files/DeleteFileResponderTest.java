// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class DeleteFileResponderTest {
  public MockRequest request;
  private FitNesseContext context;

  @Before
  public void setUp() {
    FileUtil.makeDir(FitNesseUtil.base);
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(FitNesseUtil.base);
  }

  @Test
  public void testDelete() throws Exception {
    File file = new File(FitNesseUtil.base + "/testfile");
    assertTrue(file.createNewFile());
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "testfile");
    request.setResource("");
    Response response = responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertEquals(303, response.getStatus());
    assertEquals("/", response.getHeader("Location"));
  }

  @Test
  public void testDeleteDirectory() throws Exception {
    File dir = new File(FitNesseUtil.base + "/dir");
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
