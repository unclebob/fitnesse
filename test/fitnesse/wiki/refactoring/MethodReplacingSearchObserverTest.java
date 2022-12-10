package fitnesse.wiki.refactoring;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.InMemoryPage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MethodReplacingSearchObserverTest {

  private WikiPage wikiPage;
  private MethodReplacingSearchObserver observer;

  @Test
  public void replacesPageContent() throws Exception {
    wikiPage = createPageWithContent("|methodWith noParam|");
    observer = simpleMethodReplacer();

    observer.process(wikiPage);

    assertThat(wikiPage, contentMatches("|method with no param|"));
  }

  private MethodReplacingSearchObserver simpleMethodReplacer() {
    return new MethodReplacingSearchObserver(".*", "|method with no param|");
  }

  @Test
  public void replacesPageContentWithGroups() throws Exception {

    WikiPage wikiPage1 = createPageWithContent("|update no param|");
    observer = new MethodReplacingSearchObserver("|update no param|", "|updated no param|");
    observer.process(wikiPage1);
    assertTrue(wikiPage1.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage2 = createPageWithContent("|Update      no param|");
    observer = new MethodReplacingSearchObserver("|update no param|", "|updated no param|");
    observer.process(wikiPage2);
    assertTrue(wikiPage2.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage3 = createPageWithContent("|ensure|update no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage3);
    assertTrue(wikiPage3.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage4 = createPageWithContent("|reject|update no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage4);
    assertTrue(wikiPage4.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage5 = createPageWithContent("|check|update no param|someValue|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage5);
    assertTrue(wikiPage5.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage6 = createPageWithContent("|check not|update no param|someValue|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage6);
    assertTrue(wikiPage6.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage7 = createPageWithContent("|note|update no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage7);
    assertTrue(wikiPage7.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage8 = createPageWithContent("| |update no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage8);
    assertTrue(wikiPage8.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage9 = createPageWithContent("|show|update no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage9);
    assertTrue(wikiPage9.getData().getContent().contains("|updated no param|"));

    WikiPage wikiPage10 = createPageWithContent("|$value=|update no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|updated no param|");
    observer.process(wikiPage10);
    assertTrue(wikiPage10.getData().getContent().contains("|updated no param|"));
  }

  @Test
  public void replacesMultiLinedContent() throws Exception {
    wikiPage = createPageWithContent("|update no param|" + System.lineSeparator() + "|$value=|update          no param|");
    observer = new MethodReplacingSearchObserver("|updateNoParam|", "|Updated no param|");

    observer.process(wikiPage);

    assertTrue(wikiPage.getData().getContent().contains("|Updated no param|" + System.lineSeparator() + "|$value=|Updated no param|"));
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

      @Override
      public boolean matchesSafely(WikiPage wikiPage) {
        try {
          return wikiPage.getData().getContent().matches(String.format(".*%s.*", simpleReplacement));
        } catch (Exception e) {
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("content matching ").appendValue(simpleReplacement);
      }

    };
  }

}
