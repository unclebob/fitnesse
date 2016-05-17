// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;
import static fitnesse.responders.search.SearchResponder.*;

import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Request;
import fitnesse.http.Response;

public class SearchFormResponderTest {
  private String content;
 
  @Before
  public void setUp() throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    SearchResponder responder = new SearchResponder();
    MockRequest request = new MockRequest();
    request.addInput(Request.NOCHUNK, "");
    Response response =  responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    content = sender.sentData();

  }
     
  public void testFocusOnSearchBox() throws Exception {
    assertSubString("onload=\"document.forms[0].searchString.focus()\"", content);
  }

  @Test
  public void testHtml() throws Exception {
    assertHasRegexp("form", content);
    assertHasRegexp("input", content);
    assertSubString("<input", content);
    assertSubString("type=\"hidden\"", content);
    assertSubString("name=\"responder\"", content);
    assertSubString("value=\"search\"", content);
  }

  @Test
  public void testForTwoSearchTypes() throws Exception {
    assertSubString("type=\"submit\"", content);
    assertSubString("value=\"Search Titles\"", content);
    assertSubString("value=\"Search Content\"", content);
  }

  @Test
  public void propertiesForm() throws Exception {
    assertHasRegexp("<input.*value=\"Search Properties\".*>", content);
    assertHasRegexp("<input.*name=\"responder\".*value=\"searchProperties\"", content);

    for (String attributeName : SEARCH_ACTION_ATTRIBUTES) {
      assertSubString(attributeName, content);
    }
  }
}
