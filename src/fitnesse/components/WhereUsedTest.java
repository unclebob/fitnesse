// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.util.ArrayList;
import java.util.List;

import util.RegexTestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class WhereUsedTest extends RegexTestCase implements SearchObserver {
  private WikiPage root;
  private InMemoryPage pageOne;
  private WikiPage pageTwo;
  private WikiPage pageThree;
  private WhereUsed whereUsed;

  private List<WikiPage> hits = new ArrayList<WikiPage>();
  private PageCrawler crawler;

  public void hit(WikiPage page) throws Exception {
    hits.add(page);
  }

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    pageOne = (InMemoryPage) crawler.addPage(root, PathParser.parse("PageOne"), "this is page one ^ChildPage");
    pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne . SomeMissingPage");
    pageThree = crawler.addPage(root, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
    crawler.addPage(pageTwo, PathParser.parse("ChildPage"), "I will be a virtual page to .PageOne ");

    whereUsed = new WhereUsed(root);
  }

  public void testFindReferencingPages() throws Exception {
    List<WikiPage> resultList = whereUsed.findReferencingPages(pageOne);
    assertEquals(2, resultList.size());
    assertEquals(pageTwo, resultList.get(0));

    resultList = whereUsed.findReferencingPages(pageTwo);
    assertEquals(1, resultList.size());

    resultList = whereUsed.findReferencingPages(pageThree);
    assertEquals(0, resultList.size());
  }

  public void testObserving() throws Exception {
    clearPreviousSearchResults();
    whereUsed.searchForReferencingPages(pageOne, this);
    assertEquals(2, hits.size());
  }

  public void testOnlyOneReferencePerPage() throws Exception {
    WikiPage newPage = crawler.addPage(root, PathParser.parse("NewPage"), "one reference to PageThree.  Two reference to PageThree");
    List<WikiPage> resultList = whereUsed.findReferencingPages(pageThree);
    assertEquals(1, resultList.size());
    assertEquals(newPage, resultList.get(0));
  }

  public void testWordsNotFoundInPreprocessedText() throws Exception {
    crawler.addPage(root, PathParser.parse("NewPage"), "{{{ PageThree }}}");
    List<WikiPage> resultList = whereUsed.findReferencingPages(pageThree);
    assertEquals(0, resultList.size());
  }

  public void testDontLookForReferencesInVirtualPages() throws Exception {
    clearPreviousSearchResults();
    FitNesseUtil.bindVirtualLinkToPage(pageOne, pageTwo);
    whereUsed = new WhereUsed(pageOne);
    whereUsed.searchForReferencingPages(pageOne, this);
    assertEquals(0, hits.size());
  }

  private void clearPreviousSearchResults() {
    hits.clear();
  }

}
