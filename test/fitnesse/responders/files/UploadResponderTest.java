// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.UploadedFile;
import fitnesse.testutil.FitNesseUtil;

public class UploadResponderTest {
  private FitNesseContext context;
  private UploadResponder responder;
  private MockRequest request;
  private File testFile;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext(null);
    FileUtil.makeDir(context.getRootPagePath());
    FileUtil.makeDir(context.getRootPagePath() + "/files");
    testFile = FileUtil.createFile(context.getRootPagePath() + "/tempFile.txt", "test content");

    responder = new UploadResponder();
    request = new MockRequest();
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(context.getRootPagePath());
  }

  @Test
  public void testMakeResponse() throws Exception {
    request.addInput("file", new UploadedFile("sourceFilename.txt", "plain/text", testFile));
    request.setResource("files/");

    Response response = responder.makeResponse(context, request);

    File file = new File(context.getRootPagePath() + "/files/sourceFilename.txt");
    assertTrue(file.exists());
    assertEquals("test content", FileUtil.getFileContent(file));

    assertEquals(303, response.getStatus());
    assertEquals("/files/", response.getHeader("Location"));
  }

  @Test
  public void testMakeResponseSpaceInFileName() throws Exception {
    request.addInput("file", new UploadedFile("source filename.txt", "plain/text", testFile));
    request.setResource("files/");

    Response response = responder.makeResponse(context, request);

    File file = new File(context.getRootPagePath() + "/files/source filename.txt");
    assertTrue(file.exists());
    assertEquals("test content", FileUtil.getFileContent(file));

    assertEquals(303, response.getStatus());
    assertEquals("/files/", response.getHeader("Location"));
  }

  @Test
  public void testMakeResponseSpaceInDirectoryName() throws Exception {
    FileUtil.makeDir(context.getRootPagePath() + "/files/Folder With Space");
    request.addInput("file", new UploadedFile("filename.txt", "plain/text", testFile));
    request.setResource("files/Folder%20With%20Space/");

    Response response = responder.makeResponse(context, request);

    File file = new File(context.getRootPagePath() + "/files/Folder With Space/filename.txt");
    assertTrue(file.exists());
    assertEquals("test content", FileUtil.getFileContent(file));

    assertEquals(303, response.getStatus());
    assertEquals("/files/Folder%20With%20Space/", response.getHeader("Location"));
  }

  @Test
  public void testMakeRelativeFilename() throws Exception {
    String name1 = "name1.txt";
    String name2 = "name2";
    String name3 = "C:\\folder\\name3.txt";
    String name4 = "/home/user/name4.txt";

    assertEquals("name1.txt", UploadResponder.makeRelativeFilename(name1));
    assertEquals("name2", UploadResponder.makeRelativeFilename(name2));
    assertEquals("name3.txt", UploadResponder.makeRelativeFilename(name3));
    assertEquals("name4.txt", UploadResponder.makeRelativeFilename(name4));
  }

  @Test
  public void testMakeNewFilename() throws Exception {
    assertEquals("file_copy1.txt", UploadResponder.makeNewFilename("file.txt", 1));
    assertEquals("file_copy2.txt", UploadResponder.makeNewFilename("file.txt", 2));
    assertEquals("a.b.c.d_copy2.txt", UploadResponder.makeNewFilename("a.b.c.d.txt", 2));
    assertEquals("somefile_copy1", UploadResponder.makeNewFilename("somefile", 1));
  }
}
