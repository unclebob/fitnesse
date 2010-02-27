package fitnesse.responders.refactoring;

import fitnesse.components.ContentReplacingSearchObserver;
import fitnesse.components.PageFinder;
import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.SearchObserver;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

public class SearchReplaceResponder extends ResultResponder {

  private PageFinder finder;
  private SearchObserver observer;

  protected String getPageFooterInfo(int hits) throws Exception {
    return String.format("Replaced %d matches for your search.", hits);
  }

  protected String getTitle() throws Exception {
    return String.format("Replacing matching content \"%s\" with content \"%s\"",
        getSearchString(), getReplacementString());
  }

  private String getReplacementString() {
    return (String) request.getInput("replacementString");
  }

  private String getSearchString() {
    return (String) request.getInput("searchString");
  }

  public void hit(WikiPage page) throws Exception {
    observer.hit(page);
    super.hit(page);
  }

  protected void startSearching() throws Exception {
    super.startSearching();
    String searchString = getSearchString();
    String replacementString = getReplacementString();

    observer = new ContentReplacingSearchObserver(searchString, replacementString);
    finder = new RegularExpressionWikiPageFinder(searchString, this);
    finder.search(page);
  }

}
