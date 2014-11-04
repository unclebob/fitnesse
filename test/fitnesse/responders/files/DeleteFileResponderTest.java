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
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeleteFileResponderTest {
  public MockRequest request;
  private FitNesseContext context;

  @Before
  public void setUp() {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.destroyTestContext(context);
  }

  @Test
  public void testDelete() throws Exception {
    File dir = new File(context.getRootPagePath(), "files");
    dir.mkdirs();
    File file = new File(dir, "testfile");
    assertTrue(file.createNewFile());
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "testfile");
    request.setResource("files/");
    Response response = responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertEquals(303, response.getStatus());
    assertEquals("/files/", response.getHeader("Location"));
  }

  @Test
  public void testDeleteDirectory() throws Exception {
    File dir = new File(context.getRootPagePath() + "/files/dir");
    assertTrue(dir.mkdirs());
    File file = new File(dir, "testChildFile");
    assertTrue(file.createNewFile());
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "dir");
    request.setResource("files/");
    responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertFalse(dir.exists());
  }

  @Test
  public void canNotDeleteFileOutsideFilesSection() throws Exception {
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "../../dir");
    request.setResource("files/");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertTrue(response.getContent(), response.getContent().contains("Invalid path: dir"));
  }

  @Test
  public void canNotDeleteFileOutsideFilesSectionWithInvalidResource() throws Exception {
    DeleteFileResponder responder = new DeleteFileResponder();
    request.addInput("filename", "dir");
    request.setResource("files/../../");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertTrue(response.getContent(), response.getContent().contains("Invalid path: dir"));
  }


}
