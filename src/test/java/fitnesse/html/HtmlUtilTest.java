// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class HtmlUtilTest {

  private WikiPage root;
  private FitNesseContext context;

  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("root");
    context = FitNesseUtil.makeTestContext(root);
  }

  @Test
  public void testMakeDivTag() {
    String expected = "<div class=\"myClass\"></div>" + HtmlElement.endl;
    assertEquals(expected, HtmlUtil.makeDivTag("myClass").html());
  }

  @Test
  public void testMakeDefaultActions() {
    String pageName = "SomePage";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, "SomePage");
  }

  @Test
  public void testMakeActionsWithTestButtonWhenNameStartsWithTest() {
    String pageName = "TestSomething";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  @Test
  public void testMakeActionsWithSuffixButtonWhenNameEndsWithTest() {
    String pageName = "SomethingTest";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  @Test
  public void testMakeActionsWithSuiteButtonWhenNameStartsWithSuite() {
    String pageName = "SuiteNothings";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
  }

  @Test
  public void testMakeActionsWithSuiteButtonWhenNameEndsWithSuite() {
    String pageName = "NothingsSuite";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
  }

  private String getActionsHtml(String pageName) {
    root.addChildPage(pageName);
    HtmlPage htmlPage = context.pageFactory.newPage();
    htmlPage.setNavTemplate("wikiNav.vm");
    htmlPage.put("actions", new WikiPageActions(root.getChildPage(pageName)));
    return htmlPage.html();
  }

  private void verifyDefaultLinks(String html, String pageName) {
    assertSubString("<a href=\"" + pageName + "?edit\" accesskey=\"e\">Edit</a>", html);
    assertSubString("<a href=\"" + pageName + "?versions\" accesskey=\"v\">Versions</a>", html);
    assertSubString("<a href=\"" + pageName + "?properties\" accesskey=\"p\">Properties</a>", html);
    assertSubString("<a href=\"" + pageName + "?refactor&amp;type=rename\">Rename</a>", html);
    assertSubString("<a href=\"" + pageName + "?whereUsed\" accesskey=\"w\">Where Used</a>", html);
    assertSubString("<a href=\"/files\" accesskey=\"f\">Files</a>", html);
    assertSubString("<a href=\"?searchForm\" accesskey=\"s\">Search</a>", html);
    assertSubString("<a href=\".FitNesse.UserGuide\" accesskey=\"\">User Guide</a>", html);
  }

  @Test
  public void testMakeReplaceElementScript() {
    String newText = "<p>My string has \"quotes\" and \r \n</p>";
    HtmlTag scriptTag = HtmlUtil.makeReplaceElementScript("element-name", newText);
    String expected = "<script>document.getElementById(\"element-name\").innerHTML = " +
    		"\"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";</script>";
    assertSubString(expected, scriptTag.html());
  }

  @Test
  public void testMakeToggleClassScript() {
    HtmlTag scriptTag = HtmlUtil.makeToggleClassScript("some-id", "some-class");
    String expected = "<script>$(\"#some-id\").toggleClass(\"some-class\");</script>";
    assertSubString(expected, scriptTag.html());
  }

  @Test
  public void testMakeInitErrorMetadataScript() {
    HtmlTag scriptTag = HtmlUtil.makeInitErrorMetadataScript();
    String expected = "<script>initErrorMetadata();</script>";
    assertSubString(expected, scriptTag.html());
  }

  @Test
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

  @Test
  public void testMakeSilentLink() {
    HtmlTag tag = HtmlUtil.makeSilentLink("test?responder", new RawHtml("string with \"quotes\""));
    assertSubString("<a href=\"#\" onclick=\"doSilentRequest('test?responder')\">string with \"quotes\"</a>", tag.html());
  }
}
