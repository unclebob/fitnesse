// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.testsystems.Descriptor;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class WikiPageDescriptorTest {


  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
  }

  @Test
  public void buildFullySpecifiedTestSystemNameForDebugRun() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor wikiPageDescriptor = new WikiPageDescriptor(testPage.readOnlyData(), true, false, "");
    String testSystemType = wikiPageDescriptor.getTestSystemType();
    Assert.assertEquals("system", testSystemType);
  }

  @Test
  public void buildFullySpecifiedTestSystemNameAndIdentifierForDebugRun() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system:A}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor wikiPageDescriptor = new WikiPageDescriptor(testPage.readOnlyData(), true, false, "");
    String testSystemType = wikiPageDescriptor.getTestSystemType();
    Assert.assertEquals("system", testSystemType);
  }


  @Test
  public void buildTestSystemTypeIsFit() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "");
    WikiPageDescriptor wikiPageDescriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, false, new ClassPathBuilder().getClasspath(testPage));
    String testSystemType = wikiPageDescriptor.getTestSystemType();
    Assert.assertEquals("fit", testSystemType);
  }

  private String getClassPath(WikiPage page) {
    return new ClassPathBuilder().getClasspath(page);
  }

  private WikiPage makeTestPage(String pageText) {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    return WikiPageUtil.addPage(root, PathParser.parse("TestPage"), pageText);
  }

  @Test
  public void testReadSystemPropertyIfPagePropertyDoesNotExist() {
    String pageText = "!define TEST_PROPERTY {foo}\n";
    System.setProperty("test.property", "bar");
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, false, getClassPath(page));
    assertEquals("foo", descriptor.getVariable("TEST_PROPERTY"));
    assertEquals("bar", descriptor.getVariable("test.property"));
  }

  @Test
  public void testPageVariableTakesPrecedenceOverSystemProperty() {
    String pageText = "!define TEST_PROPERTY {foo}\n";
    System.setProperty("TEST_PROPERTY", "bar");
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, false, getClassPath(page));
    assertEquals("foo", descriptor.getVariable("TEST_PROPERTY"));
  }


}
