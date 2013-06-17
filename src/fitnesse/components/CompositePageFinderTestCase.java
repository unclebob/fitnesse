package fitnesse.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;

import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class CompositePageFinderTestCase {

  protected PageFinder delegate;
  protected CompositePageFinder sut;
  protected WikiPage page;
  protected WikiPage pageOne;
  protected WikiPage pageTwo;
  protected WikiPage pageThree;

  public CompositePageFinderTestCase() {
    super();
  }

  @Before
  public void init() throws Exception {
    delegate = mock(PageFinder.class);
    page = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(page, PathParser.parse("PageOne"), "this is page one ^ChildPage");
    pageTwo = WikiPageUtil.addPage(page, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne . SomeMissingPage");
    pageThree = WikiPageUtil.addPage(page, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), "I will be a virtual page to .PageOne ");
  }

  protected void setupMockWithEmptyReturnValue() throws Exception {
    when(delegate.search(any(WikiPage.class))).thenReturn(
        new ArrayList<WikiPage>());
  }

  protected List<WikiPage> setupWikiPageList(WikiPage... pages) {
    List<WikiPage> returned = new ArrayList<WikiPage>();
    for (WikiPage page : pages) {
      returned.add(page);
    }
    return returned;
  }

  protected void assertFoundResultsEqualsExpectation(List<WikiPage> expected2, List<WikiPage> results) {
    assertEquals(expected2.size(), results.size());
    assertTrue(results.containsAll(expected2));
  }

}