// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.reporting.JavascriptUtil;
import fitnesse.responders.WikiPageActions;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertSubString;

public class HtmlUtilTest {

  private FitNesseContext context;

  @Before
  public void setUp() {
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void testMakeDivTag() {
    String expected = "<div class=\"myClass\"></div>" + HtmlElement.endl;
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("class", "myClass");
    div.add("");
    assertEquals(expected, div.html());
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
    assertSubString("<a class=\"nav-link text-secondary\" href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  @Test
  public void testMakeActionsWithSuffixButtonWhenNameEndsWithTest() {
    String pageName = "SomethingTest";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a class=\"nav-link text-secondary\" href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  @Test
  public void testMakeActionsWithSuiteButtonWhenNameStartsWithSuite() {
    String pageName = "SuiteNothings";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a class=\"nav-link text-secondary\" href=\"" + pageName + "?suite\" accesskey=\"t\">Suite</a>", html);
  }

  @Test
  public void testMakeActionsWithSuiteButtonWhenNameEndsWithSuite() {
    String pageName = "NothingsSuite";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a class=\"nav-link text-secondary\" href=\"" + pageName + "?suite\" accesskey=\"t\">Suite</a>", html);
  }

  @Test
  public void shouldEscapeOnlyXmlCharacters() {
    assertEquals("ab&amp;cd&lt;ef&gt;", HtmlUtil.escapeHTML("ab&cd<ef>"));
  }

  @Test
  public void shouldEscapeMultipleOccurencesOfTheSameCharacter() {
    assertEquals("ab&amp;cd&amp;ef&amp;", HtmlUtil.escapeHTML("ab&cd&ef&"));
  }

  @Test
  public void shouldUnescape() {
    assertEquals("& < > &lt; &gt; &amp;", HtmlUtil.unescapeHTML("&amp; &lt; &gt; &amp;lt; &amp;gt; &amp;amp;"));
  }

  private String getActionsHtml(String pageName) {
    WikiPageUtil.addPage(context.getRootPage(), PathParser.parse(pageName), "");
    HtmlPage htmlPage = context.pageFactory.newPage();
    htmlPage.setNavTemplate("wikiNav.vm");
    htmlPage.put("actions", new WikiPageActions(context.getRootPage().getChildPage(pageName)));
    return htmlPage.html(null);
  }

  private void verifyDefaultLinks(String html, String pageName) {
    assertSubString("<a class=\"nav-link text-secondary\" href=\"" + pageName + "?edit\" accesskey=\"e\">Edit</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"" + pageName + "?versions\" accesskey=\"v\">Versions</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"" + pageName + "?properties\" accesskey=\"p\">Properties</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"" + pageName + "?refactor&amp;type=rename\">Rename</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"" + pageName + "?whereUsed\" accesskey=\"w\">Where Used</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"/files\" accesskey=\"f\">Files</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"?search\" accesskey=\"s\">Search</a>", html);
    assertSubString("<a class=\"dropdown-item\" href=\"/FitNesse.UserGuide\" accesskey=\"\">User Guide</a>", html);
  }

  @Test
  public void testMakeReplaceElementScript() {
    String newText = "<p>My string has \"quotes\" and \r \n</p>";
    HtmlTag scriptTag = JavascriptUtil.makeReplaceElementScript("element-name", newText);
    String expected = "<script>document.getElementById(\"element-name\").innerHTML = " +
    		"\"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";</script>";
    assertSubString(expected, scriptTag.html());
  }

  @Test
  public void testMakeInitErrorMetadataScript() {
    HtmlTag scriptTag = JavascriptUtil.makeInitErrorMetadataScript();
    String expected = "<script>initErrorMetadata();</script>";
    assertSubString(expected, scriptTag.html());
  }

  @Test
  public void testMakeAppendElementScript() {
    String appendText = "<p>My string has \"quotes\" and \r \n</p>";
    HtmlTag scriptTag = JavascriptUtil.makeAppendElementScript("element-name", appendText);
    String expected1 = "<script>var existingContent = document.getElementById(\"element-name\").innerHTML;";
    String expected2 = "document.getElementById(\"element-name\").innerHTML = " +
      "existingContent + \"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";";
    String expected3 =  "</script>";
    assertSubString(expected1, scriptTag.html());
    assertSubString(expected2, scriptTag.html());
    assertSubString(expected3, scriptTag.html());
  }

  @Test
  public void shouldEscapeBackslashesInMakeAppendElementScript() {
    String appendText = "<p>My string has escaped \\r \\n</p>";
    HtmlTag scriptTag = JavascriptUtil.makeAppendElementScript("element\\r\\n\\", appendText);
    assertSubString("element\\\\r\\\\n\\\\", scriptTag.html());
    assertSubString("My string has escaped \\\\r \\\\n", scriptTag.html());
  }


  @Test
  public void shouldEscapeBackslashesInMakeReplaceElementScript() {
    String appendText = "<p>My string has escaped \\r \\n</p>";
    HtmlTag scriptTag = JavascriptUtil.makeReplaceElementScript("element\\r\\n\\", appendText);
    assertSubString("element\\\\r\\\\n\\\\", scriptTag.html());
    assertSubString("My string has escaped \\\\r \\\\n", scriptTag.html());
  }

  @Test
  public void testMakeSilentLink() {
    HtmlTag tag = JavascriptUtil.makeSilentLink("test?responder", new RawHtml("string with \"quotes\""));
    assertSubString("<a href=\"#\" onclick=\"doSilentRequest('test?responder')\">string with \"quotes\"</a>", tag.html());
  }
}
