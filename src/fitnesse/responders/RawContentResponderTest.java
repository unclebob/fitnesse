// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static util.RegexTestCase.assertSubString;

import fitnesse.Responder;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Test;

public class RawContentResponderTest extends ResponderTestCase {
  protected Responder responderInstance() {
    return new RawContentResponder();
  }

  @Test
  public void testSimplePage() throws Exception {
    String result = getResultsUsing("simple content");
    assertSubString("simple content", result);
  }

  @Test
  public void testNoHtmlRendered() throws Exception {
    String result = getResultsUsing("'''simple content'''");
    assertSubString("'''simple content'''", result);
  }

  private String getResultsUsing(String content) throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("SimplePage"), content);
    request.setResource("SimplePage");
    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String result = sender.sentData();
    return result;
  }
}
