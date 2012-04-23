// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.templateUtilities;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlElement;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import util.RegexTestCase;

public class HtmlPageTest extends RegexTestCase {
  private static final String endl = HtmlElement.endl;

  private HtmlPage page;
  private String html;

  private FitNesseContext context;

  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext(null);
    page = new HtmlPage(context.pageFactory.getVelocityEngine(), "skeleton.vm", "fitnesse_theme");
    html = page.html();
  }

  public void tearDown() throws Exception {
  }

  public void testStandardTags() throws Exception {
    assertTrue("bad doctype for page: " + html, html.startsWith("<!DOCTYPE html>"));
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
    assertSubString("href=\"/files/fitnesse/css/fitnesse_theme.css\"", html);
    assertSubString("src=\"/files/fitnesse/javascript/fitnesse.js\"", html);
    assertSubString("src=\"/files/fitnesse/javascript/fitnesse_theme.js\"", html);
  }

  public void testIncludesBody() throws Exception {
    assertSubString("<body>", html);
    assertSubString("</body>", html);
  }

  public void testIncludesHeading() throws Exception {
    assertSubString("<header>", html);
  }

  public void testMainBar() throws Exception {
    assertSubString("<article>", html);
    String mainHtml = page.html();
    assertSubString("<header>", mainHtml);
    assertSubString("<article>", mainHtml);
  }

  public void testSidebar() throws Exception {
    assertSubString("<nav>", html);
  }

  public void testBreadCrumbsWithCurrentPageLinked() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle(PathParser.parse(trail)));
    String breadcrumbs = page.html();
    assertSubString("<a href=\"/TstPg1\">TstPg1</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2\">TstPg2</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3\">TstPg3</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3.TstPg4\">TstPg4</a>", breadcrumbs);
  }

  public void testBreadCrumbsWithCurrentPageNotLinked() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle(PathParser.parse(trail)).notLinked());
    String breadcrumbs = page.html();
    assertSubString("<a href=\"/TstPg1\">TstPg1</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2\">TstPg2</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3\">TstPg3</a>", breadcrumbs);
    assertHasRegexp("<h1>\\s*TstPg4\\s*</h1>", breadcrumbs);
  }

  public void testBreadCrumbsWithPageType() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle("Some Type", PathParser.parse(trail)));
    String breadcrumbs = page.html();
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3.TstPg4\">TstPg4</a>", breadcrumbs);
  }


}
