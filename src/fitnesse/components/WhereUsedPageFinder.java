package fitnesse.components;

import java.util.ArrayList;
import java.util.List;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WidgetVisitor;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.PreProcessorLiteralWidget;
import fitnesse.wikitext.widgets.PreformattedWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class WhereUsedPageFinder implements TraversalListener, SearchObserver, WidgetVisitor, PageFinder {
  private WikiPage subjectPage;
  private SearchObserver observer;
  private WikiPage currentPage;

  private List<WikiPage> hits = new ArrayList<WikiPage>();

  public WhereUsedPageFinder(WikiPage subjectPage, SearchObserver observer) {
    this.subjectPage = subjectPage;
    this.observer = observer;
  }

  public void hit(WikiPage referencingPage) throws Exception {
  }

  public void visit(WikiWidget widget) throws Exception {
  }

  public void visit(WikiWordWidget widget) throws Exception {
    if (hits.contains(currentPage))
      return;
    WikiPage referencedPage = widget.getReferencedPage();
    if (referencedPage != null && referencedPage.equals(subjectPage)) {
      hits.add(currentPage);
      observer.hit(currentPage);
    }
  }

  public void visit(AliasLinkWidget widget) throws Exception {
  }

  @SuppressWarnings("unchecked")
  public void processPage(WikiPage currentPage) throws Exception {
    this.currentPage = currentPage;
    String content = currentPage.getData().getContent();
    WidgetBuilder referenceWidgetBuilder = new WidgetBuilder(new Class[]{PreProcessorLiteralWidget.class, WikiWordWidget.class, PreformattedWidget.class});
    ParentWidget widgetRoot = new WidgetRoot(content, currentPage, referenceWidgetBuilder);
    widgetRoot.acceptVisitor(this);
  }

  public List<WikiPage> search(WikiPage page) throws Exception {
    hits.clear();
    subjectPage.getPageCrawler().traverse(page, this);
    return hits;
  }

  public String getSearchPattern() throws Exception {
    return subjectPage.getName();
  }

}
