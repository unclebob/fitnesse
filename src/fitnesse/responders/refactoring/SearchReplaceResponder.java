package fitnesse.responders.refactoring;

import fitnesse.components.ContentReplacingSearchObserver;
import fitnesse.components.PageFinder;
import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.SearchObserver;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

public class SearchReplaceResponder extends ResultResponder implements SearchObserver {

  private PageFinder finder;
  private SearchObserver contentReplaceObserver;
  private SearchObserver webOutputObserver;

  protected String getPageFooterInfo(int hits) {
    return String.format("Replaced %d matches for your search.", hits);
  }

  protected String getTitle() {
    return String.format("Replacing matching content \"%s\" with content \"%s\"",
        getSearchString(), getReplacementString());
  }

  private String getReplacementString() {
    return (String) request.getInput("replacementString");
  }

  private String getSearchString() {
    return (String) request.getInput("searchString");
  }

  public void hit(WikiPage page) {
    contentReplaceObserver.hit(page);
    webOutputObserver.hit(page);
  }

  @Override
  protected void startSearching(SearchObserver observer) {
    webOutputObserver = observer;
    String searchString = getSearchString();
    String replacementString = getReplacementString();

    contentReplaceObserver = new ContentReplacingSearchObserver(searchString, replacementString);
    finder = new RegularExpressionWikiPageFinder(searchString, this);
    finder.search(page);
  }

}
