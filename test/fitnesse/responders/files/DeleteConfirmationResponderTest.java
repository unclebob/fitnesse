// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

public class DeleteConfirmationResponderTest {
  MockRequest request;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void testContentOfPage() throws Exception {
    request.setResource("files");
    request.addInput("filename", "MyFile.txt");
    Responder responder = new DeleteConfirmationResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String content = response.getContent();

    assertSubString("deleteFile", content);
    assertSubString("Delete File", content);
    assertSubString("MyFile.txt", content);
  }

}
