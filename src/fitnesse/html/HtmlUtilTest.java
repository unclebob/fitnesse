// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.HtmlPageFactory;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.MockSocket;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;

public class HtmlUtilTest extends RegexTestCase {

  private WikiPage root;

  public void setUp() {
    root = InMemoryPage.makeRoot("root");
    FitNesseUtil.makeTestContext(root);
  }

  public void testMakeFormTag() {
    HtmlTag formTag = HtmlUtil.makeFormTag("method", "action");
    assertSubString("method", formTag.getAttribute("method"));
    assertSubString("action", formTag.getAttribute("action"));
  }

  public void testMakeDivTag() {
    String expected = "<div class=\"myClass\"></div>" + HtmlElement.endl;
    assertEquals(expected, HtmlUtil.makeDivTag("myClass").html());
  }

  public void testMakeDefaultActions() {
    String pageName = "SomePage";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, "SomePage");
  }

  public void testMakeActionsWithTestButtonWhenNameStartsWithTest() {
    String pageName = "TestSomething";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  public void testMakeActionsWithSuffixButtonWhenNameEndsWithTest() {
    String pageName = "SomethingTest";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  public void testMakeActionsWithSuiteButtonWhenNameStartsWithSuite() {
    String pageName = "SuiteNothings";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
  }

  public void testMakeActionsWithSuiteButtonWhenNameEndsWithSuite() {
    String pageName = "NothingsSuite";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
  }

  private String getActionsHtml(String pageName) {
    root.addChildPage(pageName);
    HtmlPage htmlPage = new HtmlPageFactory().newPage();
    htmlPage.actions = new WikiPageActions(root.getChildPage(pageName));
    return htmlPage.html();
  }

  private void verifyDefaultLinks(String html, String pageName) {
    assertSubString("<a href=\"" + pageName + "?edit\" accesskey=\"e\">Edit</a>", html);
    assertSubString("<a href=\"" + pageName + "?versions\" accesskey=\"v\">Versions</a>", html);
    assertSubString("<a href=\"" + pageName + "?properties\" accesskey=\"p\">Properties</a>", html);
    assertSubString("<a href=\"" + pageName + "?refactor\" accesskey=\"r\">Refactor</a>", html);
    assertSubString("<a href=\"" + pageName + "?whereUsed\" accesskey=\"w\">Where Used</a>", html);
    assertSubString("<a href=\"/files\" accesskey=\"f\">Files</a>", html);
    assertSubString("<a href=\"?searchForm\" accesskey=\"s\">Search</a>", html);
    assertSubString("<a href=\".FitNesse.UserGuide\" accesskey=\"\">User Guide</a>", html);
  }

  public void testMakeReplaceElementScript() {
    String newText = "<p>My string has \"quotes\" and \r \n</p>";
    HtmlTag scriptTag = HtmlUtil.makeReplaceElementScript("element-name", newText);
    String expected = "<script>document.getElementById(\"element-name\").innerHTML = " +
    		"\"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";</script>";
    assertSubString(expected, scriptTag.html());
  }
  
  public void testMakeAppendElementScript() {
    String appendText = "<p>My string has \"quotes\" and \r \n</p>";
    HtmlTag scriptTag = HtmlUtil.makeAppendElementScript("element-name", appendText);
    String expected1 = "<script>var existingContent = document.getElementById(\"element-name\").innerHTML;"; 
    String expected2 = "document.getElementById(\"element-name\").innerHTML = " + 
      "existingContent + \"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";";
    String expected3 =  "</script>";
    assertSubString(expected1, scriptTag.html());
    assertSubString(expected2, scriptTag.html());
    assertSubString(expected3, scriptTag.html());
  }
  
  public void testMakeSilentLink() {
    HtmlTag tag = HtmlUtil.makeSilentLink("test?responder", new RawHtml("string with \"quotes\""));
    assertSubString("<a href=\"#\" onclick=\"doSilentRequest('test?responder')\">string with \"quotes\"</a>", tag.html());
  }
}
