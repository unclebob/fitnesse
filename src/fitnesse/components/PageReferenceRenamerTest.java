// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import util.RegexTestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class PageReferenceRenamerTest extends RegexTestCase {
  WikiPage root;
  WikiPage subWiki;
  WikiPage subWiki_pageOne;
  WikiPage subWiki_pageTwo;
  WikiPage subWiki_pageTwo_pageTwoChild;

  PageReferenceRenamer renamer;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    subWiki = crawler.addPage(root, PathParser.parse("SubWiki"), "");
    subWiki_pageTwo = crawler.addPage(subWiki, PathParser.parse("PageTwo"), "");
    subWiki_pageTwo_pageTwoChild = crawler.addPage(subWiki_pageTwo, PathParser.parse("PageTwoChild"), "");
  }

  public void testReferencesOnPageOne_1() throws Exception {
    checkChangesOnPageOne("Stuff PageTwo Stuff\n", "Stuff PageThree Stuff\n");
  }

  public void testReferencesOnPageOne_2() throws Exception {
    checkChangesOnPageOne("Stuff !-PageTwo-! Stuff\n", "Stuff !-PageTwo-! Stuff\n");
  }

  public void testReferencesOnPageOne_3() throws Exception {
    checkChangesOnPageOne("Stuff PageOne.PageTwo Stuff\n", "Stuff PageOne.PageTwo Stuff\n");
  }

  public void testReferencesOnPageOne_4() throws Exception {
    checkChangesOnPageOne("Stuff .SubWiki.PageTwo.PageTwoChild Stuff\n", "Stuff .SubWiki.PageThree.PageTwoChild Stuff\n");
  }

  public void testReferencesOnPageOne_5() throws Exception {
    checkChangesOnPageOne("Stuff ^PageTwo Stuff\n", "Stuff ^PageTwo Stuff\n");
  }

  public void testReferencesOnPageOne_6() throws Exception {
    checkChangesOnPageOne("# Stuff PageTwo Stuff\n", "# Stuff PageTwo Stuff\n");
  }

  public void testReferencesOnPageOne_7() throws Exception {
    checkChangesOnPageOne("{{{Stuff PageTwo Stuff}}}\n", "{{{Stuff PageTwo Stuff}}}\n");
  }

  public void testReferencesOnPageOne_8() throws Exception {
    checkChangesOnPageOne("Stuff .SubWiki.PageTwo Stuff\n", "Stuff .SubWiki.PageThree Stuff\n");
  }

  public void testReferencesOnPageOne_9() throws Exception {
    checkChangesOnPageOne("Stuff .SubWiki.PageTwo.NoPage Stuff\n", "Stuff .SubWiki.PageThree.NoPage Stuff\n");
  }

  public void testTestReferencesToSubWiki_1() throws Exception {
    PageData data = subWiki.getData();
    data.setContent("Stuff >PageTwo Stuff\n");
    subWiki.commit(data);

    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();

    String updatedSubWikiContent = subWiki.getData().getContent();
    assertEquals("Stuff >PageThree Stuff\n", updatedSubWikiContent);
  }

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
    subWiki_pageOne = crawler.addPage(subWiki, PathParser.parse("PageOne"), beforeText);
    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();
    subWiki_pageOne = subWiki.getChildPage("PageOne");
    String updatedPageOneContent = subWiki_pageOne.getData().getContent();
    assertEquals(expectedAfterText, updatedPageOneContent);
  }

  public void testRenameParentPage() throws Exception {
    PageData pageTwoChildData = subWiki_pageTwo_pageTwoChild.getData();
    pageTwoChildData.setContent("gunk .SubWiki.PageTwo gunk");
    subWiki_pageTwo_pageTwoChild.commit(pageTwoChildData);
    renamer = new PageReferenceRenamer(root, subWiki_pageTwo, "PageThree");
    renamer.renameReferences();
    String updatedContent = subWiki_pageTwo_pageTwoChild.getData().getContent();
    assertEquals("gunk .SubWiki.PageThree gunk", updatedContent);
  }

  public void testSubPageReferenceUnchangedWhenParentRenamed() throws Exception {
    WikiPage pageOne = crawler.addPage(subWiki, PathParser.parse("PageOne"), "gunk ^SubPage gunk");
    renamer = new PageReferenceRenamer(root, subWiki, "RenamedSubWiki");
    renamer.renameReferences();
    String updatedContent = pageOne.getData().getContent();
    assertEquals("gunk ^SubPage gunk", updatedContent);
  }

  public void testRenameParentWithSubPageReferenceOnSibling() throws Exception {
    WikiPage pageOne = crawler.addPage(subWiki, PathParser.parse("PageOne"), "gunk PageTwo gunk");
    renamer = new PageReferenceRenamer(root, subWiki, "RenamedSubWiki");
    renamer.renameReferences();
    String updatedContent = pageOne.getData().getContent();
    assertEquals("gunk PageTwo gunk", updatedContent);
  }

  public void testRenameSiblingOfRoot() throws Exception {
    WikiPage source = crawler.addPage(root, PathParser.parse("SourcePage"), "gunk TargetPage gunk");
    WikiPage target = crawler.addPage(root, PathParser.parse("TargetPage"));
    renamer = new PageReferenceRenamer(root, target, "RenamedPage");
    renamer.renameReferences();
    String updatedSourceContent = source.getData().getContent();
    assertEquals("gunk RenamedPage gunk", updatedSourceContent);
  }

  public void testRenameSubpageOfRoot() throws Exception {
    WikiPage source = crawler.addPage(root, PathParser.parse("SourcePage"), "gunk ^TargetPage gunk");
    WikiPage target = crawler.addPage(source, PathParser.parse("TargetPage"));
    renamer = new PageReferenceRenamer(root, target, "RenamedPage");
    renamer.renameReferences();
    String updatedSourceContent = source.getData().getContent();
    assertEquals("gunk >RenamedPage gunk", updatedSourceContent);
  }

  public void testImageNotChanged() throws Exception {
    final String IMAGE_WIDGET = "!img http://PageTwo.jpg";
    checkChangesOnPageOne(IMAGE_WIDGET, IMAGE_WIDGET);
  }

  public void testLinkNotChanged() throws Exception {
    final String LINK_WIDGET = "http://PageTwo";
    checkChangesOnPageOne(LINK_WIDGET, LINK_WIDGET);
  }

  public void testPathNotChanged() throws Exception {
    final String PATH_WIDGET = "!path PageTwo";
    checkChangesOnPageOne(PATH_WIDGET, PATH_WIDGET);
  }

  public void testAliasTagNotChanged() throws Exception {
    final String ALIAS_LINK = "[[PageTwo][MyPageTwo]]";
    checkChangesOnPageOne(ALIAS_LINK, ALIAS_LINK);
  }

  public void testAliasLinkRenamed() throws Exception {
    checkChangesOnPageOne("gunk [[gunk][PageTwo]] gunk", "gunk [[gunk][PageThree]] gunk");
  }

  public void testAliasLinWithLiteralDoesntGetCorrupted() throws Exception {
    checkChangesOnPageOne("gunk [[!-gunk-!][PageTwo]] gunk", "gunk [[!-gunk-!][PageThree]] gunk");
  }

  public void testXrefWidgetRenamed() throws Exception {
    checkChangesOnPageOne("!see PageTwo", "!see PageThree");
  }
}

