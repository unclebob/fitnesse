package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TableScanner implements Iterable<Table> {
  private static final Class[] tableWidgets = new Class[]{IncludeWidget.class, TableWidget.class};
  private List<Table> tables = new ArrayList<Table>();
  public ParentWidget widgetRoot;


  public TableScanner(PageData data) throws Exception {
    WikiPage page = data.getWikiPage();
    widgetRoot = new WidgetRoot(data.getContent(), page, new WidgetBuilder(tableWidgets));
    scanParentForTables(widgetRoot);
  }

  private void scanParentForTables(ParentWidget parent) {
    List<WikiWidget> widgets = parent.getChildren();
    for (WikiWidget widget : widgets) {
      if (widget instanceof TableWidget)
        tables.add(new Table((TableWidget) widget));
      else if (widget instanceof ParentWidget)
        scanParentForTables((ParentWidget) widget);
    }
  }

  public int getTableCount() {
    return tables.size();
  }

  public Table getTable(int i) {
    return tables.get(i);
  }

  public Iterator<Table> iterator() {
    return tables.iterator();
  }

  public String toWikiText() {
    StringBuffer wikiText = new StringBuffer();
    appendChildWikiText(wikiText, widgetRoot);
    return wikiText.toString();
  }

  private void appendChildWikiText(StringBuffer wikiText, ParentWidget parent) {
    for (WikiWidget widget : parent.getChildren()) {
      appendWidgetText(wikiText, widget);
      if (widget instanceof IncludeWidget)
        surroundTextWithIncludedDiv(wikiText, widget);
      else if (widget instanceof CollapsableWidget)
        appendChildWikiText(wikiText, (ParentWidget) widget);
    }
  }

  private void surroundTextWithIncludedDiv(StringBuffer wikiText, WikiWidget widget) {
    IncludeWidget iw = (IncludeWidget) widget;
    wikiText.append(String.format("!-<div class=\"included\">-!!note Included !-%s-!\n", iw.getPageName()));
    appendChildWikiText(wikiText, (ParentWidget) widget);
    wikiText.append("!-</div>-!\n");
  }

  private void appendWidgetText(StringBuffer wikiText, WikiWidget widget) {
    try {
      wikiText.append(widget.asWikiText());
    } catch (Exception e) {
      wikiText.append(String.format("Can't append widget text %s, %s\n", e.getClass().getName(), e.getMessage()));
    }
  }
}
