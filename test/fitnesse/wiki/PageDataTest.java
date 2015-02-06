// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static fitnesse.wiki.PageData.LAST_MODIFYING_USER;
import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;

import fitnesse.wiki.fs.InMemoryPage;
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
    String html = page.getHtml();
    assertHasRegexp("''italics''", html);
    assertHasRegexp("<i>italics</i>", html);
  }

  @Test
  public void testVariablesWithinVariablesAreResolved() throws Exception {
    String text = "!define x {b}\n!define y (a${x}c)\n${y}";
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), text);
    String html = page.getHtml();
    assertHasRegexp("abc", html);
    assertHasRegexp("variable defined: y=a\\$\\{x\\}c", html);
    String variableContents = page.getVariable("y");
    assertEquals("abc", variableContents);
  }

  @Test
  public void testLiteral() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("LiteralPage"), "!-literal-!");
    String renderedContent = page.getHtml();
    assertHasRegexp("literal", renderedContent);
    assertDoesntHaveRegexp("!-literal-!", renderedContent);
  }

  @Test
  public void testVariableIgnoredInParentPreformatted() throws Exception {  //--variables in parent preformatted blocks must not recognize !define widgets.
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage parent = WikiPageUtil.addPage(root, PathParser.parse("VariablePage"), "{{{\n!define SOMEVAR {A VALUE}\n}}}\n");
    WikiPage child = WikiPageUtil.addPage(parent, PathParser.parse("ChildPage"), "${SOMEVAR}\n");
    String renderedContent = child.getHtml();
    assertHasRegexp("undefined variable", renderedContent);
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
    
    assertEquals(pageWithUnixLineEndings.getHtml(), pageWithDosLineEndings.getHtml());
  }
}
