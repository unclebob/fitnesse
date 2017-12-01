package fitnesse.responders.refactoring;

import fitnesse.wiki.refactoring.ContentReplacingSearchObserver;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.RegularExpressionWikiPageFinder;
import fitnesse.components.TraversalListener;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

public class SearchReplaceResponder extends ResultResponder {

  protected String getPageFooterInfo(int hits) {
    return String.format("Replaced %d matches for your search.", hits);
  }

  @Override
  protected String getTemplate() {
    return "searchResults";
  }

  @Override
  protected String getTitle() {
    return String.format("Replacing matching content \"%s\" with content \"%s\"",
        getSearchString(), getReplacementString());
  }

  @Override
  protected PageFinder getPageFinder(TraversalListener<WikiPage> webOutputObserver) {
    String searchString = getSearchString();
    String replacementString = getReplacementString();

    ContentReplacingSearchObserver contentReplaceObserver =
            new ContentReplacingSearchObserver(searchString, replacementString);

    return new RegularExpressionWikiPageFinder(searchString,
            new SearchReplaceTraverser(contentReplaceObserver, webOutputObserver));
  }

  private String getReplacementString() {
    return request.getInput("replacementString");
  }

  private String getSearchString() {
    return request.getInput("searchString");
  }
}


class SearchReplaceTraverser  implements TraversalListener<WikiPage> {

  private TraversalListener<? super WikiPage> contentReplaceObserver;
  private TraversalListener<? super WikiPage> webOutputObserver;

  public SearchReplaceTraverser(TraversalListener<? super WikiPage> contentReplaceObserver, TraversalListener<? super WikiPage> webOutputObserver) {
    this.contentReplaceObserver = contentReplaceObserver;
    this.webOutputObserver = webOutputObserver;
  }

  @Override
  public void process(WikiPage page) {
    contentReplaceObserver.process(page);
    webOutputObserver.process(page);
  }

}
