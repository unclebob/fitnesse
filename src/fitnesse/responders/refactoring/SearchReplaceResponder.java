package fitnesse.responders.refactoring;

import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.refactoring.ChangedLines;
import fitnesse.wiki.refactoring.ContentReplacer;
import fitnesse.wiki.refactoring.ContentReplacingSearchObserver;
import fitnesse.wiki.refactoring.MethodReplacingSearchObserver;
import fitnesse.wiki.search.MethodWikiPageFinder;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.RegularExpressionWikiPageFinder;

public class SearchReplaceResponder extends ResultResponder {

  private ContentReplacer replacer;

  protected String getPageFooterInfo(int hits) {
    return String.format("Replaced %d matches for your search.", hits);
  }

  @Override
  protected String getTemplate() {
    return "searchResults";
  }

  @Override
  protected String getTitle() {
    String title = String.format("Replacing matching content \"%s\" with content \"%s\"",
      getSearchString(), getReplacementString());
    return isDryRun() ? "Dry run (nothing is saved): " + title : title;
  }

  @Override
  protected PageFinder getPageFinder(TraversalListener<WikiPage> webOutputObserver) {
    String searchString = getSearchString();
    String replacementString = getReplacementString();

    if (isMethodReplace()) {
      MethodReplacingSearchObserver methodReplaceObserver =
        new MethodReplacingSearchObserver(searchString, replacementString);
      replacer = methodReplaceObserver;

      return new MethodWikiPageFinder(searchString, replacingObserver(methodReplaceObserver, webOutputObserver));
    } else {
      ContentReplacingSearchObserver contentReplaceObserver =
        new ContentReplacingSearchObserver(searchString, replacementString);
      replacer = contentReplaceObserver;
      return new RegularExpressionWikiPageFinder(searchString, replacingObserver(contentReplaceObserver, webOutputObserver));
    }
  }

  private TraversalListener<WikiPage> replacingObserver(TraversalListener<WikiPage> replaceObserver,
                                                       TraversalListener<WikiPage> webOutputObserver) {
    // a dry run only shows the pages that would change
    return isDryRun() ? webOutputObserver : new SearchReplaceTraverser(replaceObserver, webOutputObserver);
  }

  @Override
  public List<ChangedLines> getChangedLines(WikiPage page) {
    if (!isDryRun() || replacer == null) {
      return null;
    }
    String content = page.getData().getContent();
    return ChangedLines.between(content, replacer.replace(content));
  }

  @Override
  public int getSearchThreads() {
    // replace changes pages while traversing, so pages must not be loaded ahead
    return 1;
  }

  private boolean isDryRun() {
    return request.hasInput("dryRun");
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


