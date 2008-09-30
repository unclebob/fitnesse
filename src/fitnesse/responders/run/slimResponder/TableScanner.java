package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.TableWidget;
import fitnesse.wikitext.widgets.WidgetRoot;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class TableScanner implements Iterable<Table> {
  private static final Class[] tableWidgets = new Class[]{ TableWidget.class};
  private List<Table> tables = new ArrayList<Table>();
  public ParentWidget widgetRoot;


  public TableScanner(PageData data) throws Exception {
    WikiPage page = data.getWikiPage();
    widgetRoot = new WidgetRoot(data.getContent(), page, new WidgetBuilder(tableWidgets));
    List<WikiWidget> widgets = widgetRoot.getChildren();
    for (WikiWidget widget : widgets) {
      if (widget instanceof TableWidget)
        tables.add(new Table((TableWidget) widget));
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
    for (WikiWidget widget : widgetRoot.getChildren())
      appendWidgetText(wikiText, widget);
    return wikiText.toString();
  }

  private void appendWidgetText(StringBuffer wikiText, WikiWidget widget){
    try {
      wikiText.append(widget.asWikiText());
    } catch (Exception e) {
      wikiText.append(String.format("Can't append widget text %s, %s\n", e.getClass().getName(), e.getMessage()));
    }
  }
}
