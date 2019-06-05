// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html.template;

import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.ConfigurationParameter;
import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import org.junit.Before;
import org.junit.Test;

public class HtmlPageTest {

  private HtmlPage page;
  private String html;

  @Before
  public void setUp() throws Exception {
    Properties properties = new Properties();
    properties.setProperty(ConfigurationParameter.THEME.getKey(), "fitnesse_straight");
    FitNesseContext context = FitNesseUtil.makeTestContext(properties);
    page = new HtmlPage(context.pageFactory.getVelocityEngine(), "skeleton.vm", "fitnesse_theme", "/");
    html = page.html(null);
  }

  @Test
  public void testStandardTags() throws Exception {
    assertTrue("bad doctype for page: " + html, html.startsWith("<!DOCTYPE html>"));
    assertSubString("<html>", html);
    assertHasRegexp("</html>", html);
  }

  @Test
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

  @Test
  public void testIncludesBody() throws Exception {
    assertSubString("<body>", html);
    assertSubString("</body>", html);
  }

  @Test
  public void testIncludesHeading() throws Exception {
    assertSubString("<header>", html);
  }

  @Test
  public void testMainBar() throws Exception {
    assertSubString("<article>", html);
    String mainHtml = page.html(null);
    assertSubString("<header>", mainHtml);
    assertSubString("<article>", mainHtml);
  }

  @Test
  public void testSidebar() throws Exception {
    assertSubString("<nav>", html);
  }

  @Test
  public void testBreadCrumbsWithCurrentPageLinked() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle(PathParser.parse(trail)));
    String breadcrumbs = page.html(null);
    assertSubString("<a href=\"/TstPg1\">TstPg1</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2\">TstPg2</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3\">TstPg3</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3.TstPg4\">TstPg4</a>", breadcrumbs);
  }

  @Test
  public void testBreadCrumbsWithCurrentPageNotLinked() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle(PathParser.parse(trail)).notLinked());
    String breadcrumbs = page.html(null);
    assertSubString("<a href=\"/TstPg1\">TstPg1</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2\">TstPg2</a>", breadcrumbs);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3\">TstPg3</a>", breadcrumbs);
    assertHasRegexp("<h1>\\s*TstPg4\\s*</h1>", breadcrumbs);
  }

  @Test
  public void testBreadCrumbsWithPageType() throws Exception {
    String trail = "TstPg1.TstPg2.TstPg3.TstPg4";
    page.setPageTitle(new PageTitle("Some Type", PathParser.parse(trail)));
    String breadcrumbs = page.html(null);
    assertSubString("<a href=\"/TstPg1.TstPg2.TstPg3.TstPg4\">TstPg4</a>", breadcrumbs);
  }

}
