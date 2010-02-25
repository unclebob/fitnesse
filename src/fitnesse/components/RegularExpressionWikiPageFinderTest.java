package fitnesse.components;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

public class RegularExpressionWikiPageFinderTest implements SearchObserver {

  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage childPage;
  private WikiPage virtualPage;
  private PageCrawler crawler;

  List<WikiPage> foundPages = new ArrayList<WikiPage>();
  private RegularExpressionWikiPageFinder pageFinder;

  public void hit(WikiPage page) throws Exception {
    foundPages.add(page);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
    childPage = crawler.addPage(root, PathParser.parse("PageOne.PageOneChild"),
        "PageChild is a child of PageOne");
    virtualPage = crawler.addPage(root, PathParser.parse("PageTwo"),
        "PageTwo has a bit of content too\n^PageOneChild");
    PageData data = virtualPage.getData();
    data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://localhost:"
        + FitNesseUtil.port + "/PageOne");
    virtualPage.commit(data);
    foundPages.clear();
  }

  @Test
  public void pagesNotMatching() throws Exception {
    pageFinder = pageFinder(notMatchingSearchText());

    pageFinder.search(root);

    assertThat(foundPages, isEmpty());
  }

  @Test
  public void singlePageMatches() throws Exception {
    pageFinder = pageFinder(matchTextForPageOne());

    pageFinder.search(root);

    assertThat(foundPages, found(pageOne));
  }

  @Test
  public void multiplePageMatch() throws Exception {
    pageFinder = pageFinder(matchAll());

    pageFinder.search(root);

    assertThat(foundPages, found(root, pageOne, childPage, virtualPage));
  }

  @Test
  public void matchesSublevels() throws Exception {
    pageFinder = pageFinder(matchAll());

    pageFinder.search(pageOne);

    assertThat(foundPages, found(pageOne, childPage));
  }

  private String matchAll() {
    return ".*";
  }

  private String matchTextForPageOne() {
    return "PageOne content";
  }

  private String notMatchingSearchText() {
    return "this search text does not match any page";
  }

  private RegularExpressionWikiPageFinder pageFinder(String searchText) {
    return new RegularExpressionWikiPageFinder(searchText, this);
  }

  private Matcher<List<WikiPage>> found(final WikiPage... pages) {
    return new TypeSafeMatcher<List<WikiPage>>() {

      public boolean matchesSafely(List<WikiPage> foundPages) {
        if (foundPages.size() != pages.length) return false;

        for (WikiPage expectedPage: pages) {
          if (!foundPages.contains(expectedPage)) return false;
        }
        return true;
      }

      public void describeTo(Description description) {
        description.appendText("a list containing ").appendValue(pages);
      }
    };
  }

  private Matcher<List<WikiPage>> isEmpty() {
    return new EmptyListMatcher();
  }

  private static class EmptyListMatcher extends TypeSafeMatcher<List<WikiPage>> {
    public boolean matchesSafely(List<WikiPage> pages) {
      return pages.isEmpty();
    }

    public void describeTo(Description description) {
      description.appendText("an empty list");
    }
  }
}
