// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import fitnesse.http.SimpleResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;

public class RenameFileResponderTest {
  private MockRequest request;
  private FitNesseContext context;

  @Before
  public void setUp() {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
    FileUtil.makeDir(context.getRootPagePath());
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(context.getRootPagePath());
  }

  @Test
  public void testMakeResponse() throws Exception {
    File files = new File(context.getRootPagePath(), "files");
    assertTrue(files.mkdirs());
    File file = new File(files, "testfile");
    assertTrue(file.createNewFile());
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "testfile");
    request.addInput("newName", "newName");
    request.setResource("files/");
    Response response = responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertTrue(new File(context.getRootPagePath() + "/files/newName").exists());
    assertEquals(303, response.getStatus());
    assertEquals("/files/", response.getHeader("Location"));
  }

  @Test
  public void testRenameWithTrailingSpace() throws Exception {
    File files = new File(context.getRootPagePath(), "files");
    files.mkdirs();
    File file = new File(files, "testfile");
    file.createNewFile();
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "testfile");
    request.addInput("newName", "new Name With Space ");
    request.setResource("files/");
    responder.makeResponse(context, request);
    assertFalse(file.exists());
    assertTrue(new File(context.getRootPagePath() + "/files/new Name With Space").exists());
  }

  @Test
  public void canNotRenameFileFromLocationOutsideFilesSection() throws Exception {
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "oldname");
    request.addInput("newName", "newname");
    request.setResource("files/../../");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertTrue(response.getContent(), response.getContent().contains("Invalid path: files/../../"));
  }


  @Test
  public void canNotRenameToFileOutsideFilesSection() throws Exception {
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "oldname");
    request.addInput("newName", "../../newname");
    request.setResource("files/");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertTrue(response.getContent(), response.getContent().contains("Invalid path: ../../newname"));
  }

  @Test
  public void canNotRenameFilesOutsideFilesSection() throws Exception {
    RenameFileResponder responder = new RenameFileResponder();
    request.addInput("filename", "../../oldname");
    request.addInput("newName", "newname");
    request.setResource("files/");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertTrue(response.getContent(), response.getContent().contains("Invalid path: ../../oldname"));
  }


}
