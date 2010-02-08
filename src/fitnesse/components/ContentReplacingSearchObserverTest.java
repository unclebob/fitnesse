package fitnesse.components;

import static org.junit.Assert.*;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class ContentReplacingSearchObserverTest {

  private WikiPage wikiPage;
  private ContentReplacingSearchObserver observer;

  @Test
  public void replacesPageContent() throws Exception {
    wikiPage = createPageWithContent("simplePageContent");
    observer = simpleContentReplacer();

    observer.hit(wikiPage);

    assertThat(wikiPage, contentMatches("replacement"));
  }

  private ContentReplacingSearchObserver simpleContentReplacer() {
    return new ContentReplacingSearchObserver(".*", "replacement");
  }

  @Test
  public void replacesPageContentWithGroups() throws Exception {
    wikiPage = createPageWithContent("pattern1 some various text pattern2 pattern3");
    observer = new ContentReplacingSearchObserver("pattern1(.*)pattern2 pattern3", "replacement1$1replacement2 replacement3");

    observer.hit(wikiPage);

    assertThat(wikiPage, contentMatches("replacement1 some various text replacement2 replacement3"));
  }

  @Test
  public void replacesMultiLinedContent() throws Exception {
    wikiPage = createPageWithContent(multiLineContent());
    observer = new ContentReplacingSearchObserver("matching line", "replaced line");

    observer.hit(wikiPage);

    assertThat(wikiPage, contentMatches("line 1\nline 2\nline 3\nreplaced line\nline 5\nreplaced line\nline 7"));
  }

  private String multiLineContent() {
    return "line 1\nline 2\nline 3\nmatching line\nline 5\nmatching line\nline 7";
  }

  private WikiPage createPageWithContent(String pageContent) throws Exception {
    WikiPage wikiPage = InMemoryPage.makeRoot("wikiPage");
    PageData pageData = wikiPage.getData();
    pageData.setContent(pageContent);
    wikiPage.commit(pageData);
    return wikiPage;
  }

  private Matcher<WikiPage> contentMatches(final String simpleReplacement) {
    return new TypeSafeMatcher<WikiPage>() {

      public boolean matchesSafely(WikiPage wikiPage) {
        try {
          return wikiPage.getData().getContent().matches(String.format(".*%s.*", simpleReplacement));
        } catch (Exception e) {
          return false;
        }
      }

      public void describeTo(Description description) {
        description.appendText("content matching ").appendValue(simpleReplacement);
      }

    };
  }

}
