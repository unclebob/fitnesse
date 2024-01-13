package fitnesse.responders.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.refactoring.ContentReplacingSearchObserver;
import fitnesse.wiki.refactoring.MethodReplacingSearchObserver;
import fitnesse.wiki.search.MethodWikiPageFinder;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.RegularExpressionWikiPageFinder;

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

    if (isMethodReplace()) {
      MethodReplacingSearchObserver methodReplaceObserver =
        new MethodReplacingSearchObserver(searchString, replacementString);

      return new MethodWikiPageFinder(searchString,
        new SearchReplaceTraverser(methodReplaceObserver, webOutputObserver));
    } else {
      ContentReplacingSearchObserver contentReplaceObserver =
        new ContentReplacingSearchObserver(searchString, replacementString);
      return new RegularExpressionWikiPageFinder(searchString,
        new SearchReplaceTraverser(contentReplaceObserver, webOutputObserver));
    }
  }

  private boolean isMethodReplace() {
    return request.hasInput("isMethodReplace");
  }

  private String getReplacementString() {
    return request.getInput("replacementString");
  }

  private String getSearchString() {
    return request.getInput("searchString");
  }
}


