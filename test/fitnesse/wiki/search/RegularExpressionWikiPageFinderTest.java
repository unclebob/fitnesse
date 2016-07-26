package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RegularExpressionWikiPageFinderTest implements TraversalListener<WikiPage> {

  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage childPage;
  private WikiPage virtualPage;

  List<WikiPage> foundPages = new ArrayList<>();
  private WikiPageFinder pageFinder;

  @Override
  public void process(WikiPage page) {
    foundPages.add(page);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
    childPage = WikiPageUtil.addPage(root, PathParser.parse("PageOne.PageOneChild"),
            "PageChild is a child of PageOne");
    virtualPage = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"),
            "PageTwo has a bit of content too\n^PageOneChild");
    PageData data = virtualPage.getData();
    virtualPage.commit(data);
    foundPages.clear();
  }

  @Test
  public void searcher() throws Exception {
    pageFinder = pageFinder("has");
    pageFinder.search(root);
    assertThat(foundPages, found(pageOne, virtualPage));
  }

  @Test
  public void searcherAgain() throws Exception {
    pageFinder = pageFinder("a");
    pageFinder.search(root);
    assertThat(foundPages, found(pageOne, childPage, virtualPage));
  }

  @Test
  public void dontSearchProxyPages() throws Exception {
    pageFinder = pageFinder("a");
    pageFinder.search(virtualPage);
    assertEquals(1, foundPages.size());
  }

  @Test
  public void observing() throws Exception {
    pageFinder = pageFinder("has");
    pageFinder.search(root);
    assertEquals(2, foundPages.size());
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

  private WikiPageFinder pageFinder(String searchText) {
    return new RegularExpressionWikiPageFinder(searchText, this);
  }

  private Matcher<List<WikiPage>> found(final WikiPage... pages) {
    return new TypeSafeMatcher<List<WikiPage>>() {

      @Override
      public boolean matchesSafely(List<WikiPage> foundPages) {
        if (foundPages.size() != pages.length) return false;

        for (WikiPage expectedPage: pages) {
          if (!foundPages.contains(expectedPage)) return false;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a list containing ").appendValue(pages);
      }
    };
  }

  private Matcher<List<WikiPage>> isEmpty() {
    return new EmptyListMatcher();
  }

  private static class EmptyListMatcher extends TypeSafeMatcher<List<WikiPage>> {
    @Override
    public boolean matchesSafely(List<WikiPage> pages) {
      return pages.isEmpty();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("an empty list");
    }
  }
}
