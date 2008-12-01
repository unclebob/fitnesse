package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableScanner implements Iterable<Table> {
  private static final Class<?>[] tableWidgets = new Class<?>[]{IncludeWidget.class, TableWidget.class};
  private List<Table> tables = new ArrayList<Table>();
  public ParentWidget widgetRoot;


  public TableScanner(PageData data) throws Exception {
    WikiPage page = data.getWikiPage();
    String content = data.getContent();
    content = replaceVariables(page, content);
    content = removeUnprocessedLiteralsInTables(content);
    content = replaceEvaluations(new WidgetRoot("", page), content);
    widgetRoot = new WidgetRoot(content, page, new WidgetBuilder(tableWidgets));
    replaceAllProcessedLiterals(widgetRoot);
    scanParentForTables(widgetRoot);
  }

  private void replaceAllProcessedLiterals(WikiWidget widget) {
    if (widget instanceof TextWidget) {
      replaceProcessedLiterals((TextWidget) widget);
    } else if (widget instanceof ParentWidget) {
      ParentWidget parent = (ParentWidget) widget;
      List<WikiWidget> children = parent.getChildren();
      for (WikiWidget child : children) {
        replaceAllProcessedLiterals(child);
      }
    }
  }

  private static void replaceProcessedLiterals(TextWidget textWidget) {
    String text = textWidget.getRawText();
    Matcher matcher = LiteralWidget.pattern.matcher(text);
    while (matcher.find()) {
      int literalNumber = Integer.parseInt(matcher.group(1));
      String replacement = String.format("!-%s-!", textWidget.getParent().getLiteral(literalNumber));
      text = text.replace(matcher.group(), replacement);
      matcher = LiteralWidget.pattern.matcher(text);
    }
    textWidget.setText(text);
  }

  static String removeUnprocessedLiteralsInTables(String text) {
    Pattern inTablePattern = Pattern.compile("\\|[^\n\r]*!-(.*?)-![^\n\r]*\\|");
    Matcher inTableMatcher = inTablePattern.matcher(text);
    Matcher literalMatcher = PreProcessorLiteralWidget.pattern.matcher(text);
    while (inTableMatcher.find()) {
      literalMatcher.region(inTableMatcher.start(), inTableMatcher.end());
      if (literalMatcher.find()) {
        String replacement = literalMatcher.group(1);
        String firstPart = text.substring(0, literalMatcher.start());
        String lastPart = text.substring(literalMatcher.end());
        text = firstPart + replacement + lastPart;
      }
      inTableMatcher = inTablePattern.matcher(text);
      literalMatcher = PreProcessorLiteralWidget.pattern.matcher(text);
    }

    return text;
  }

  private String replaceVariables(WikiPage page, String content) throws Exception {
    Matcher matcher = VariableWidget.pattern.matcher(content);
    while (matcher.find()) {
      String replacement = page.getData().getVariable(matcher.group(1));
      if (replacement != null)
        content = content.replace(matcher.group(), replacement);
    }
    return content;
  }

  private String replaceEvaluations(ParentWidget root, String text) {
    Matcher matcher = EvaluatorWidget.pattern.matcher(text);
    while (matcher.find()) {
      EvaluatorWidget evaluatorWidget = new EvaluatorWidget(root, matcher.group());
      String replacement = null;
      try {
        replacement = evaluatorWidget.render();
      } catch (Exception e) {
        replacement = "INVALID EXPRESSION";
      }
      text = text.replace(matcher.group(), replacement);
    }
    return text;
  }

  private void scanParentForTables(ParentWidget parent) {
    List<WikiWidget> widgets = parent.getChildren();
    for (WikiWidget widget : widgets) {
      if (widget instanceof TableWidget)
        tables.add(new WikiWidgetTable((TableWidget) widget));
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
