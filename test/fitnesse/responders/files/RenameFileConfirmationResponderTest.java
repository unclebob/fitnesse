// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

import static util.RegexTestCase.assertSubString;

public class RenameFileConfirmationResponderTest {
  MockRequest request;
  private FitNesseContext context;
  private String content;

  @Before
  public void setUp() throws Exception {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void testContentOfPage() throws Exception {
    getContentForSimpleRename();

    assertSubString("renameFile", content);
    assertSubString("Rename", content);
    assertSubString("Rename <b>MyFile.txt</b>", content);
  }

  @Test
  public void testExistingFilenameIsInTextField() throws Exception {
    getContentForSimpleRename();

    assertSubString("<input id=\"rename-file\" type=\"text\" name=\"newName\" value=\"MyFile.txt\"/>", content);
  }

  private void getContentForSimpleRename() throws Exception {
    request.setResource("files");
    request.addInput("filename", "MyFile.txt");
    Responder responder = new RenameFileConfirmationResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    content = response.getContent();
  }

  @Test
  public void testFitnesseLook() throws Exception {
    Responder responder = new RenameFileConfirmationResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String content = response.getContent();
    assertSubString("<link rel=\"stylesheet\" type=\"text/css\" href=\"/files/fitnesse/css/fitnesse_wiki.css\"", content);
  }

}
