package fitnesse.responders.refactoring;

import fitnesse.wiki.refactoring.ContentReplacingSearchObserver;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.RegularExpressionWikiPageFinder;
import fitnesse.components.TraversalListener;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

public class SearchReplaceResponder extends ResultResponder implements TraversalListener<WikiPage> {

  private TraversalListener<? super WikiPage> contentReplaceObserver;
  private TraversalListener<? super WikiPage> webOutputObserver;

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
  public void traverse(TraversalListener<Object> observer) {
    webOutputObserver = observer;
    String searchString = getSearchString();
    String replacementString = getReplacementString();

    contentReplaceObserver = new ContentReplacingSearchObserver(searchString, replacementString);
    PageFinder finder = new RegularExpressionWikiPageFinder(searchString, this);
    finder.search(page);
  }

}
