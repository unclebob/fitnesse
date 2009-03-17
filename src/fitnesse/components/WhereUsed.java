// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
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

public class WhereUsed implements FitNesseTraversalListener, SearchObserver, WidgetVisitor {
  private WikiPage root;
  private WikiPage subjectPage;
  private SearchObserver observer;
  private WikiPage currentPage;

  private List<WikiPage> hits = new ArrayList<WikiPage>();

  public WhereUsed(WikiPage root) {
    this.root = root;
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

  public void searchForReferencingPages(WikiPage subjectPage, SearchObserver observer) throws Exception {
    this.observer = observer;
    this.subjectPage = subjectPage;
    root.getPageCrawler().traverse(root, this);
  }

  public List<WikiPage> findReferencingPages(WikiPage subjectPage) throws Exception {
    hits.clear();
    searchForReferencingPages(subjectPage, this);
    return hits;
  }

  public void processPage(WikiPage currentPage) throws Exception {
    this.currentPage = currentPage;
    String content = currentPage.getData().getContent();
    WidgetBuilder referenceWidgetBuilder = new WidgetBuilder(new Class[]{PreProcessorLiteralWidget.class, WikiWordWidget.class, PreformattedWidget.class});
    ParentWidget widgetRoot = new WidgetRoot(content, currentPage, referenceWidgetBuilder);
    widgetRoot.acceptVisitor(this);
  }

  public String getSearchPattern() throws Exception {
    return subjectPage.getName();
  }
}
