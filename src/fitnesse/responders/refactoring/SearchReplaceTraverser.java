package fitnesse.responders.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

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
