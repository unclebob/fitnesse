// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.wiki.WikiPageUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestSystemBaseTest {
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
  }

  @Test
  public void buildFullySpecifiedTestSystemName() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
      "!define TEST_SYSTEM {system}\n" +
        "!define TEST_RUNNER {runner}\n");
    String testSystemName = TestSystem.getDescriptor(testPage, false).getTestSystemName();
    Assert.assertEquals("system:runner", testSystemName);
  }

  @Test
  public void buildDefaultTestSystemName() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "");
    String testSystemName = TestSystem.getDescriptor(testPage, false).getTestSystemName();
    Assert.assertEquals("fit:fit.FitServer", testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsSlim() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {slim}\n");
    String testSystemName = TestSystem.getDescriptor(testPage, false).getTestSystemName();
    Assert.assertEquals("slim:fitnesse.slim.SlimService", testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsUnknownDefaultsToFit() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {X}\n");
    String testSystemName = TestSystem.getDescriptor(testPage, false).getTestSystemName();
    Assert.assertEquals("X:fit.FitServer", testSystemName);
  }


}
