// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static fitnesse.wiki.PageData.LAST_MODIFYING_USER;
import static fitnesse.wiki.PageData.PropertyEDIT;
import static fitnesse.wiki.PageData.PropertyFILES;
import static fitnesse.wiki.PageData.PropertySEARCH;
import static fitnesse.wiki.PageData.PropertyVERSIONS;
import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;

import java.util.List;

import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class PageDataTest {
  public WikiPage page;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    page = WikiPageUtil.addPage(root, PathParser.parse("PagE"), "some content");
  }

  @Test
  public void testVariablePreprocessing() throws Exception {
    PageData d = new PageData(InMemoryPage.makeRoot("RooT").getData(), "!define x {''italic''}\n${x}\n");
    String preprocessedText = d.getContent();
    assertHasRegexp("''italic''", preprocessedText);
  }

  @Test
  public void testVariablesRenderedFirst() throws Exception {
    String text = "!define x {''italics''}\n${x}";
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), text);
    String html = page.getData().getHtml();
    assertHasRegexp("''italics''", html);
    assertHasRegexp("<i>italics</i>", html);
  }

  @Test
  public void testVariablesWithinVariablesAreResolved() throws Exception {
    String text = "!define x {b}\n!define y (a${x}c)\n${y}";
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), text);
    String html = page.getData().getHtml();
    assertHasRegexp("abc", html);
    assertHasRegexp("variable defined: y=a\\$\\{x\\}c", html);
    String variableContents = page.getData().getVariable("y");
    assertEquals("abc", variableContents);
  }

  @Test
  public void testThatSpecialCharsAreNotEscapedTwice() throws Exception {
    PageData d = new PageData(new WikiPageDummy().getData(), "<b>");
    String html = d.getHtml();
    assertEquals("&lt;b&gt;", html);
  }

  @Test
  public void testLiteral() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("LiteralPage"), "!-literal-!");
    String renderedContent = page.getData().getHtml();
    assertHasRegexp("literal", renderedContent);
    assertDoesntHaveRegexp("!-literal-!", renderedContent);
  }

  @Test
  public void testVariableIgnoredInParentPreformatted() throws Exception {  //--variables in parent preformatted blocks must not recognize !define widgets.
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage parent = WikiPageUtil.addPage(root, PathParser.parse("VariablePage"), "{{{\n!define SOMEVAR {A VALUE}\n}}}\n");
    WikiPage child = WikiPageUtil.addPage(parent, PathParser.parse("ChildPage"), "${SOMEVAR}\n");
    String renderedContent = child.getData().getHtml();
    assertHasRegexp("undefined variable", renderedContent);
  }

  @Test
  public void testGetCrossReferences() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageName"), "!see XrefPage\r\n");
    List<?> xrefs = page.getData().getXrefPages();
    assertEquals("XrefPage", xrefs.get(0));
  }

  @Test
  public void testThatExamplesAtEndOfNameSetsSuiteProperty() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageExamples"));
    PageData data = new PageData(page);
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatExampleAtBeginningOfNameSetsTestProperty() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ExamplePageExample"));
    PageData data = new PageData(page);
    assertTrue(data.hasAttribute(TEST.toString()));
  }

  @Test
  public void testThatExampleAtEndOfNameSetsTestProperty() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageExample"));
    PageData data = new PageData(page);
    assertTrue(data.hasAttribute(TEST.toString()));
  }

  @Test
  public void testThatSuiteAtBeginningOfNameSetsSuiteProperty() throws Exception {
    WikiPage suitePage1 = WikiPageUtil.addPage(root, PathParser.parse("SuitePage"));
    PageData data = new PageData(suitePage1);
    assertFalse(data.hasAttribute(TEST.toString()));
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatSuiteAtEndOfNameSetsSuiteProperty() throws Exception {
    WikiPage suitePage2 = WikiPageUtil.addPage(root, PathParser.parse("PageSuite"));
    PageData data = new PageData(suitePage2);
    assertFalse(data.hasAttribute(TEST.toString()));
    assertTrue(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatTestAtBeginningOfNameSetsTestProperty() throws Exception {
    WikiPage testPage1 = WikiPageUtil.addPage(root, PathParser.parse("TestPage"));
    PageData data = new PageData(testPage1);
    assertTrue(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testThatTestAtEndOfNameSetsTestProperty() throws Exception {
    WikiPage testPage2 = WikiPageUtil.addPage(root, PathParser.parse("PageTest"));
    PageData data = new PageData(testPage2);
    assertTrue(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testDefaultAttributes() throws Exception {
    WikiPage normalPage = WikiPageUtil.addPage(root, PathParser.parse("NormalPage"));
    WikiPage suitePage3 = WikiPageUtil.addPage(root, PathParser.parse("TestPageSuite"));
    WikiPage errorLogsPage = WikiPageUtil.addPage(root, PathParser.parse("ErrorLogs.TestPage"));
    WikiPage suiteSetupPage = WikiPageUtil.addPage(root, PathParser.parse(SUITE_SETUP_NAME));
    WikiPage suiteTearDownPage = WikiPageUtil.addPage(root, PathParser.parse(SUITE_TEARDOWN_NAME));

    PageData data = new PageData(normalPage);
    assertTrue(data.hasAttribute(PropertyEDIT));
    assertTrue(data.hasAttribute(PropertySEARCH));
    assertTrue(data.hasAttribute(PropertyVERSIONS));
    assertTrue(data.hasAttribute(PropertyFILES));
    assertFalse(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));

    data = new PageData(suitePage3);
    assertFalse(data.hasAttribute(TEST.toString()));
    assertTrue(data.hasAttribute(SUITE.toString()));

    data = new PageData(errorLogsPage);
    assertFalse(data.hasAttribute(TEST.toString()));
    assertFalse(data.hasAttribute(SUITE.toString()));

    data = new PageData(suiteSetupPage);
    assertFalse(data.hasAttribute(SUITE.toString()));

    data = new PageData(suiteTearDownPage);
    assertFalse(data.hasAttribute(SUITE.toString()));
  }

  @Test
  public void testAttributesAreTruelyCopiedInCopyConstructor() throws Exception {
    PageData data = root.getData();
    data.setAttribute(LAST_MODIFYING_USER, "Joe");
    PageData newData = new PageData(data);
    newData.setAttribute(LAST_MODIFYING_USER, "Jane");

    assertEquals("Joe", data.getAttribute(LAST_MODIFYING_USER));
  }

  @Test
  public void testAllowsContentContainingCarriageReturns() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    String content = "|a|\n|table|";
    WikiPage pageWithUnixLineEndings = WikiPageUtil.addPage(root, PathParser.parse("PageName"), content);
    
    String contentWithCarriageReturns = content.replaceAll("\n", "\r\n");
    WikiPage pageWithDosLineEndings = WikiPageUtil.addPage(root, PathParser.parse("PageName2"), contentWithCarriageReturns);
    
    assertEquals(pageWithUnixLineEndings.getData().getHtml(), pageWithDosLineEndings.getData().getHtml());
  }
}
