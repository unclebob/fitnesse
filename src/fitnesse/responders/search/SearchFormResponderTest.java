// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import static fitnesse.responders.search.SearchFormResponder.*;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

public class SearchFormResponderTest {
  private SimpleResponse response;
  private String content;

  @Before
  public void setUp() throws Exception {
    SearchFormResponder responder = new SearchFormResponder();
    response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), new MockRequest());
    content = response.getContent();
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
    assertSubString("value=\"Search Titles!\"", content);
    assertSubString("value=\"Search Content!\"", content);
  }

  @Test
  public void propertiesForm() throws Exception {
    assertHasRegexp("<input.*value=\"Search Properties\".*>", content);
    assertHasRegexp("<input.*name=\"responder\".*value=\"executeSearchProperties\"", content);

    for (String attributeName : WikiPage.ACTION_ATTRIBUTES) {
      assertAttributeCheckboxCreated(content, attributeName);
    }
  }

  private void assertAttributeCheckboxCreated(String content, String attributeName) {
    assertSubString("<input type=\"checkbox\" name=\"" + attributeName + "" +
      ATTRIBUTE + SELECTED + "\"/>", content);
    assertSubString("<input type=\"checkbox\" name=\"" + attributeName + "Value\"/>", content);
  }
}
