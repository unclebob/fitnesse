// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.templateUtilities;

import fitnesse.html.HtmlElement;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import util.RegexTestCase;

public class HtmlPageTest extends RegexTestCase {
  private static final String endl = HtmlElement.endl;

  private HtmlPage page;
  private String html;

  public void setUp() throws Exception {
    FitNesseUtil.makeTestContext(null);
    page = new HtmlPage("skeleton.vm");
    html = page.html();
  }

  public void tearDown() throws Exception {
  }

  public void testStandardTags() throws Exception {
    assertTrue("bad doctype for page: " + html, html.startsWith("<!DOCTYPE HTML PUBLIC"));
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
    String mainHtml = page.html();
    assertSubString("<div class=\"header", mainHtml);
    assertSubString("<div class=\"mainbar\"", mainHtml);
  }

  public void testSidebar() throws Exception {
    assertSubString("<div class=\"sidebar", html);
    assertSubString("<a name=\"art_niche", html);
    assertSubString("<div class=\"actions", html);
  }

  public void testMain() throws Exception {
    assertSubString("<div class=\"main", html);
  }

  public void testDivide() throws Exception {
    page.setMainTemplate("breakpoint.vm");
    page.divide();
    assertNotSubString("</html>", page.preDivision);
    assertSubString("</html>", page.postDivision);
    assertNotSubString(HtmlPage.BreakPoint, page.preDivision);
    assertNotSubString(HtmlPage.BreakPoint, page.postDivision);
  }
  
  public void testBreadCrumbsWithCurrentPageLinked() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle(PathParser.parse(trail)));
    String breadcrumbs = page.html();
    String expected = getBreadCrumbsWithLastOneLinked();
    assertSubString(expected, breadcrumbs);
  }

  public void testBreadCrumbsWithCurrentPageNotLinked() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle(PathParser.parse(trail)).notLinked());
    String breadcrumbs = page.html();
    String expected = getBreadCrumbsWithLastOneNotLinked();
    assertSubString(expected, breadcrumbs);
  }

  public void testBreadCrumbsWithPageType() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle("Some Type", PathParser.parse(trail)));
    String breadcrumbs = page.html();
    String expected = getBreadCrumbsWithLastOneLinked() +
      "<br/><span class=\"page_type\">Some Type</span>" + endl;
    assertSubString(expected, breadcrumbs);
  }

  private String getBreadCrumbsWithLastOneLinked() {
    return getFirstThreeBreadCrumbs() +
      "<br/><a href=\"/TstPg1.TstPg2.TstPg3.TstPg4\" class=\"page_title\">TstPg4</a>" + endl;
  }

  private String getBreadCrumbsWithLastOneNotLinked() {
    return getFirstThreeBreadCrumbs() +
      "<br/><span class=\"page_title\">TstPg4</span>" + endl;
  }

  private String getFirstThreeBreadCrumbs() {
    return "<a href=\"/TstPg1\">TstPg1</a>." + endl +
      "<a href=\"/TstPg1.TstPg2\">TstPg2</a>." + endl +
      "<a href=\"/TstPg1.TstPg2.TstPg3\">TstPg3</a>." + endl;
  }


}
