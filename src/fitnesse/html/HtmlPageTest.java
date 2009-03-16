// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import util.RegexTestCase;

public class HtmlPageTest extends RegexTestCase {
  private HtmlPage page;
  private String html;

  public void setUp() throws Exception {
    page = new HtmlPage();
    html = page.html();
  }

  public void tearDown() throws Exception {
  }

  public void testStandardTags() throws Exception {
    assertTrue("bad doctype for page: " + html, html.startsWith(HtmlPage.DTD));
    assertSubString("<html>", html);
    assertHasRegexp("</html>", html);
  }

  public void testHead() throws Exception {
    assertSubString("<head>", html);
    assertSubString("</head>", html);
    assertSubString("<title>FitNesse</title>", html);
    assertSubString("<link", html);
    assertSubString("rel=\"stylesheet\"", html);
    assertSubString("type=\"text/css\"", html);
    assertSubString("href=\"/files/css/fitnesse.css\"", html);
    assertSubString("src=\"/files/javascript/fitnesse.js\"", html);
  }

  public void testIncludesBody() throws Exception {
    assertSubString("<body>", html);
    assertSubString("</body>", html);
  }

  public void testIncludesHeading() throws Exception {
    assertSubString("<div class=\"header\"", html);
  }

  public void testMainBar() throws Exception {
    assertSubString("<div class=\"mainbar\"", html);
    String mainHtml = page.mainbar.html();
    assertSubString("<div class=\"header", mainHtml);
    assertSubString("<div class=\"main\"", mainHtml);
  }

  public void testSidebar() throws Exception {
    assertSubString("<div class=\"sidebar", html);
    assertSubString("<div class=\"art_niche", html);
    assertSubString("<div class=\"actions", html);
  }

  public void testMain() throws Exception {
    assertSubString("<div class=\"main", html);
  }

  public void testDivide() throws Exception {
    page.main.use(HtmlPage.BreakPoint);
    page.divide();
    assertNotSubString("</html>", page.preDivision);
    assertSubString("</html>", page.postDivision);
    assertNotSubString(HtmlPage.BreakPoint, page.preDivision);
    assertNotSubString(HtmlPage.BreakPoint, page.postDivision);
  }
}
