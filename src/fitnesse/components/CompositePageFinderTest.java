package fitnesse.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class CompositePageFinderTest {

  PageFinder delegate;
  CompositePageFinder sut;
  WikiPage page;
  PageCrawler crawler;
  WikiPage pageOne;
  WikiPage pageTwo;
  WikiPage pageThree;

  @Before
  public void init() throws Exception {
    delegate = mock(PageFinder.class);
    sut = new CompositePageFinder();
    page = InMemoryPage.makeRoot("RooT");
    crawler = page.getPageCrawler();
    pageOne = crawler.addPage(page, PathParser.parse("PageOne"), "this is page one ^ChildPage");
    pageTwo = crawler.addPage(page, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne . SomeMissingPage");
    pageThree = crawler.addPage(page, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
    crawler.addPage(pageTwo, PathParser.parse("ChildPage"), "I will be a virtual page to .PageOne ");
  }

  private void setupMockWithEmptyReturnValue() throws Exception {
    when(delegate.search(any(WikiPage.class))).thenReturn(
        new ArrayList<WikiPage>());
  }

  @Test
  public void singlePageFinder() throws Exception {
    setupMockWithEmptyReturnValue();

    sut.add(delegate);

    sut.search(page);

    verify(delegate, times(1)).search(page);
  }

  @Test
  public void multiplePageFinder() throws Exception {
    setupMockWithEmptyReturnValue();

    sut.add(delegate);
    sut.add(delegate);
    sut.add(delegate);

    sut.search(page);

    verify(delegate, times(3)).search(page);
  }

  @Test
  public void intersectionIsFound() throws Exception {
    List<WikiPage> expected = setupWikiPageList(pageOne, pageTwo);

    when(delegate.search(any(WikiPage.class))).thenReturn(
        setupWikiPageList(pageOne, pageTwo, pageThree));

    PageFinder delegate2 = mock(PageFinder.class);
    when(delegate2.search(any(WikiPage.class))).thenReturn(expected);

    sut.add(delegate);
    sut.add(delegate2);

    List<WikiPage> results = sut.search(page);

    assertFoundResultsEqualsExpectation(expected, results);
  }

  private List<WikiPage> setupWikiPageList(WikiPage... pages) {
    List<WikiPage> returned = new ArrayList<WikiPage>();
    for (WikiPage page : pages) {
      returned.add(page);
    }
    return returned;
  }

  private void assertFoundResultsEqualsExpectation(List<WikiPage> expected2,
      List<WikiPage> results) {
    assertEquals(expected2.size(), results.size());
    assertTrue(results.containsAll(expected2));
  }

  @Test
  public void multpleIntersections() throws Exception {
    List<WikiPage> expected = setupWikiPageList(pageOne, pageTwo);

    when(delegate.search(any(WikiPage.class))).thenReturn(
        setupWikiPageList(pageOne, pageTwo, pageThree));

    PageFinder delegate2 = mock(PageFinder.class);
    when(delegate2.search(any(WikiPage.class))).thenReturn(expected);

    sut.add(delegate);
    sut.add(delegate2);
    sut.add(delegate);

    List<WikiPage> results = sut.search(page);

    assertFoundResultsEqualsExpectation(expected, results);
  }
}
