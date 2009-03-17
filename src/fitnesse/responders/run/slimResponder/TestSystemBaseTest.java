// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestSystemBaseTest {
  private WikiPage root;
  private PageCrawler crawler;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  @Test
  public void buildFullySpecifiedTestSystemName() throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"),
      "!define TEST_SYSTEM {system}\n" +
        "!define TEST_RUNNER {runner}\n");
    String testSystemName = TestSystem.getTestSystemName(testPage.getData());
    Assert.assertEquals("system:runner", testSystemName);
  }

  @Test
  public void buildDefaultTestSystemName() throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), "");
    String testSystemName = TestSystem.getTestSystemName(testPage.getData());
    Assert.assertEquals("fit:fit.FitServer", testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsSlim() throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {slim}\n");
    String testSystemName = TestSystem.getTestSystemName(testPage.getData());
    Assert.assertEquals("slim:fitnesse.slim.SlimService", testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsUnknownDefaultsToFit() throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {X}\n");
    String testSystemName = TestSystem.getTestSystemName(testPage.getData());
    Assert.assertEquals("X:fit.FitServer", testSystemName);
  }


}
