// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;

public class RenameFileConfirmationResponderTest extends RegexTestCase {
  MockRequest request;
  private FitNesseContext context;
  private String content;
  private SimpleResponse response;
  private Responder responder;

  public void setUp() throws Exception {
    request = new MockRequest();
    context = new FitNesseContext();
    context.rootPagePath = SampleFileUtility.base;
    SampleFileUtility.makeSampleFiles();
  }

  public void tearDown() throws Exception {
    SampleFileUtility.deleteSampleFiles();
  }

  public void testContentOfPage() throws Exception {
    getContentForSimpleRename();

    assertSubString("renameFile", content);
    assertSubString("Rename File", content);
    assertSubString("Rename <b>MyFile.txt</b>", content);
  }

  public void testExistingFilenameIsInTextField() throws Exception {
    getContentForSimpleRename();

    assertSubString("<input type=\"text\" name=\"newName\" value=\"MyFile.txt\"/>", content);
  }

  private void getContentForSimpleRename() throws Exception {
    request.setResource("files");
    request.addInput("filename", "MyFile.txt");
    responder = new RenameFileConfirmationResponder();
    response = (SimpleResponse) responder.makeResponse(context, request);
    content = response.getContent();
  }

  public void testFitnesseLook() throws Exception {
    Responder responder = new RenameFileConfirmationResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String content = response.getContent();
    assertSubString("<link rel=\"stylesheet\" type=\"text/css\" href=\"/files/css/fitnesse.css\" media=\"screen\"/>", content);
  }

}
