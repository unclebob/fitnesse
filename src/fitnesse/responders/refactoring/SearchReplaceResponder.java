package fitnesse.responders.refactoring;

import fitnesse.components.ContentReplacingSearchObserver;
import fitnesse.components.PageFinder;
import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.TraversalListener;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

public class SearchReplaceResponder extends ResultResponder implements TraversalListener<WikiPage> {

  private PageFinder finder;
  private TraversalListener contentReplaceObserver;
  private TraversalListener webOutputObserver;

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

  public void process(WikiPage page) {
    contentReplaceObserver.process(page);
    webOutputObserver.process(page);
  }

  @Override
  public void traverse(TraversalListener observer) {
    webOutputObserver = observer;
    String searchString = getSearchString();
    String replacementString = getReplacementString();

    contentReplaceObserver = new ContentReplacingSearchObserver(searchString, replacementString);
    finder = new RegularExpressionWikiPageFinder(searchString, this);
    finder.search(page);
  }

}
