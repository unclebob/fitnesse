// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PageReferenceRenamerTest {
  WikiPage root;
  WikiPage subWiki;
  WikiPage subWiki_pageOne;
  WikiPage subWiki_pageTwo;
  WikiPage subWiki_pageTwo_pageTwoChild;

  PageReferenceRenamer renamer;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    subWiki = WikiPageUtil.addPage(root, PathParser.parse("SubWiki"), "");
    subWiki_pageTwo = WikiPageUtil.addPage(subWiki, PathParser.parse("PageTwo"), "");
    subWiki_pageTwo_pageTwoChild = WikiPageUtil.addPage(subWiki_pageTwo, PathParser.parse("PageTwoChild"), "");
  }

  @Test
  public void testReferencesOnPageOne_1() throws Exception {
    checkChangesOnPageOne("Stuff PageTwo Stuff\n", "Stuff PageThree Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_2() throws Exception {
    checkChangesOnPageOne("Stuff !-PageTwo-! Stuff\n", "Stuff !-PageTwo-! Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_3() throws Exception {
    checkChangesOnPageOne("Stuff PageOne.PageTwo Stuff\n", "Stuff PageOne.PageTwo Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_4() throws Exception {
    checkChangesOnPageOne("Stuff .SubWiki.PageTwo.PageTwoChild Stuff\n", "Stuff .SubWiki.PageThree.PageTwoChild Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_5() throws Exception {
    checkChangesOnPageOne("Stuff ^PageTwo Stuff\n", "Stuff ^PageTwo Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_6() throws Exception {
    checkChangesOnPageOne("# Stuff PageTwo Stuff\n", "# Stuff PageTwo Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_7() throws Exception {
    checkChangesOnPageOne("{{{Stuff PageTwo Stuff}}}\n", "{{{Stuff PageTwo Stuff}}}\n");
  }

  @Test
  public void testReferencesOnPageOne_8() throws Exception {
    checkChangesOnPageOne("Stuff .SubWiki.PageTwo Stuff\n", "Stuff .SubWiki.PageThree Stuff\n");
  }

  @Test
  public void testReferencesOnPageOne_9() throws Exception {
    checkChangesOnPageOne("Stuff .SubWiki.PageTwo.NoPage Stuff\n", "Stuff .SubWiki.PageThree.NoPage Stuff\n");
  }

  @Test
  public void testTestReferencesToSubWiki_1() throws Exception {
    PageData data = subWiki.getData();
    data.setContent("Stuff >PageTwo Stuff\n");
    subWiki.commit(data);

    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();

    String updatedSubWikiContent = subWiki.getData().getContent();
    assertEquals("Stuff >PageThree Stuff\n", updatedSubWikiContent);
  }

  @Test
  public void testTestReferencesToSubWiki_2() throws Exception {
    PageData data = subWiki.getData();
    data.setContent("Stuff >PageTwo.DeepPage Stuff\n");
    subWiki.commit(data);

    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();
    String updatedSubWikiContent = subWiki.getData().getContent();
    assertEquals("Stuff >PageThree.DeepPage Stuff\n", updatedSubWikiContent);
  }

  private void checkChangesOnPageOne(String beforeText, String expectedAfterText) throws Exception {
    subWiki_pageOne = WikiPageUtil.addPage(subWiki, PathParser.parse("PageOne"), beforeText);
    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();
    subWiki_pageOne = subWiki.getChildPage("PageOne");
    String updatedPageOneContent = subWiki_pageOne.getData().getContent();
    assertEquals(expectedAfterText, updatedPageOneContent);
  }

  @Test
  public void testRenameParentPage() throws Exception {
    PageData pageTwoChildData = subWiki_pageTwo_pageTwoChild.getData();
    pageTwoChildData.setContent("gunk .SubWiki.PageTwo gunk");
    subWiki_pageTwo_pageTwoChild.commit(pageTwoChildData);
    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();
    String updatedContent = subWiki_pageTwo_pageTwoChild.getData().getContent();
    assertEquals("gunk .SubWiki.PageThree gunk", updatedContent);
  }

  @Test
  public void testSubPageReferenceUnchangedWhenParentRenamed() throws Exception {
    WikiPage pageOne = WikiPageUtil.addPage(subWiki, PathParser.parse("PageOne"), "gunk ^SubPage gunk");
    renamer = new PageReferenceRenamer(root, subWiki, "RenamedSubWiki");
    renamer.renameReferences();
    String updatedContent = pageOne.getData().getContent();
    assertEquals("gunk ^SubPage gunk", updatedContent);
  }

  @Test
  public void testRenameParentWithSubPageReferenceOnSibling() throws Exception {
    WikiPage pageOne = WikiPageUtil.addPage(subWiki, PathParser.parse("PageOne"), "gunk PageTwo gunk");
    renamer = new PageReferenceRenamer(root, subWiki, "RenamedSubWiki");
    renamer.renameReferences();
    String updatedContent = pageOne.getData().getContent();
    assertEquals("gunk PageTwo gunk", updatedContent);
  }

  @Test
  public void testRenameSiblingOfRoot() throws Exception {
    WikiPage source = WikiPageUtil.addPage(root, PathParser.parse("SourcePage"), "gunk TargetPage gunk");
    WikiPage target = WikiPageUtil.addPage(root, PathParser.parse("TargetPage"));
    renamer = new PageReferenceRenamer(root, target, "RenamedPage");
    renamer.renameReferences();
    String updatedSourceContent = source.getData().getContent();
    assertEquals("gunk RenamedPage gunk", updatedSourceContent);
  }

  @Test
  public void testRenameSubpageOfRoot() throws Exception {
    WikiPage source = WikiPageUtil.addPage(root, PathParser.parse("SourcePage"), "gunk ^TargetPage gunk");
    WikiPage target = WikiPageUtil.addPage(source, PathParser.parse("TargetPage"));
    renamer = new PageReferenceRenamer(root, target, "RenamedPage");
    renamer.renameReferences();

    source = root.getChildPage("SourcePage");
    String updatedSourceContent = source.getData().getContent();
    assertEquals("gunk >RenamedPage gunk", updatedSourceContent);
  }

  @Test
  public void testImageNotChanged() throws Exception {
    final String IMAGE_WIDGET = "!img http://PageTwo.jpg";
    checkChangesOnPageOne(IMAGE_WIDGET, IMAGE_WIDGET);
  }

  @Test
  public void testLinkNotChanged() throws Exception {
    final String LINK_WIDGET = "http://PageTwo";
    checkChangesOnPageOne(LINK_WIDGET, LINK_WIDGET);
  }

  @Test
  public void testPathNotChanged() throws Exception {
    final String PATH_WIDGET = "!path PageTwo";
    checkChangesOnPageOne(PATH_WIDGET, PATH_WIDGET);
  }

  @Test
  public void testAliasTagNotChanged() throws Exception {
    final String ALIAS_LINK = "[[PageTwo][MyPageTwo]]";
    checkChangesOnPageOne(ALIAS_LINK, ALIAS_LINK);
  }

  @Test
  public void testAliasLinkRenamed() throws Exception {
    checkChangesOnPageOne("gunk [[gunk][PageTwo]] gunk", "gunk [[gunk][PageThree]] gunk");
  }

  @Test
  public void testAliasLinWithLiteralDoesntGetCorrupted() throws Exception {
    checkChangesOnPageOne("gunk [[!-gunk-!][PageTwo]] gunk", "gunk [[!-gunk-!][PageThree]] gunk");
  }

  @Test
  public void testXrefWidgetRenamed() throws Exception {
    checkChangesOnPageOne("!see PageTwo", "!see PageThree");
  }

  @Test
  public void testSymbolicLinkGetsRenamed() {
    checkSymbolicLinkChangesOnPageOne("PageTwo", "PageThree");
  }

  @Test
  public void testSymbolicLinkToSubGetsRenamed() {
    checkSymbolicLinkChangesOnPageOne(".SubWiki.PageTwo.PageTwoChild", ".SubWiki.PageThree.PageTwoChild");
  }

  @Test
  public void testOtherSymbolicLinkDoesNotGetRenamed() {
    checkSymbolicLinkChangesOnPageOne(".SubWiki.SomeOther", ".SubWiki.SomeOther");
  }

  @Test
  public void testAllSymbolicLinksGetRenamed() {
    Map<String, String> beforeLinks = new HashMap<>();
    Map<String, String> expectedLinks = new HashMap<>();
    beforeLinks.put("FirstLink", "PageTwo");
    expectedLinks.put("FirstLink", "PageThree");
    beforeLinks.put("SecondLink", ".SubWiki.PageTwo.PageTwoChild");
    expectedLinks.put("SecondLink", ".SubWiki.PageThree.PageTwoChild");
    beforeLinks.put("ThirdLink", "PageTwo.PageTwoChild");
    expectedLinks.put("ThirdLink", "PageThree.PageTwoChild");
    beforeLinks.put("FourthLink", "AnotherLink");
    expectedLinks.put("FourthLink", "AnotherLink");

    checkSymbolicLinkChangesOnPageOne(beforeLinks, expectedLinks);
  }

  private void checkSymbolicLinkChangesOnPageOne(String beforeLink, String expectedAfterLink) {
    checkSymbolicLinkChangesOnPageOne(
      Collections.singletonMap("FirstLink", beforeLink),
      Collections.singletonMap("FirstLink", expectedAfterLink));
  }

  private void checkSymbolicLinkChangesOnPageOne(Map<String, String> beforeLinks, Map<String, String> expectedAfterLinks) {
    subWiki_pageOne = WikiPageUtil.addPage(subWiki, PathParser.parse("PageOne"), "page 1 PageTwo");
    PageData data = subWiki_pageOne.getData();
    WikiPageProperty linksProperty = new WikiPageProperty();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME, linksProperty);
    for (Map.Entry<String, String> entry : beforeLinks.entrySet()) {
      linksProperty.set(entry.getKey(), entry.getValue());
    }
    subWiki_pageOne.commit(data);

    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();

    PageData pageOneDataAfter = subWiki.getChildPage("PageOne").getData();
    linksProperty = pageOneDataAfter.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    for (Map.Entry<String, String> entry : expectedAfterLinks.entrySet()) {
      String actual = linksProperty.get(entry.getKey());
      assertEquals(entry.getValue(), actual);
    }

    // content is also done
    String updatedPageOneContent = pageOneDataAfter.getContent();
    assertEquals("page 1 PageThree", updatedPageOneContent);
  }
}

