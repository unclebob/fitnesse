// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import util.RegexTestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestRunnerTest extends RegexTestCase {
  private fitnesse.TestRunner runner;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.addPage(root, PathParser.parse("ClassPath"), "!path classes");
    crawler.addPage(root, PathParser.parse("FrontPage"), "front page");
    FitNesseUtil.startFitnesse(root);

    runner = new fitnesse.TestRunner();
  }

  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
    System.setOut(System.out);
  }

  public void testParseArgs() throws Exception {
    String[] args = new String[]{};
    assertFalse(runner.acceptAgrs(args));

    args = new String[]{"blah", "http://localhost/FrontPage"};
    assertFalse(runner.acceptAgrs(args));

    args = new String[]{"http://localhost/FrontPage"};
    assertTrue(runner.acceptAgrs(args));
    assertFalse(runner.verbose);
    assertFalse(runner.showHtml);

    args = new String[]{"-v", "-h", "http://localhost/FrontPage"};
    assertTrue(runner.acceptAgrs(args));
    assertTrue(runner.verbose);
    assertTrue(runner.showHtml);
  }
}
