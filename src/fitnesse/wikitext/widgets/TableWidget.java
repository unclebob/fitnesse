// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WikiWidget;

public class TableWidget extends ParentWidget {
  public static final String LF = LINE_BREAK_PATTERN;
  public static final String REGEXP = "^!?(?:\\|[^\r\n]*?\\|" + LF + ")+";
  private static final Pattern pattern = Pattern.compile("(!?)(\\|[^\r\n]*?)\\|" + LF);

  public boolean isLiteralTable;
  private int columns = 0;

  public int getColumns() {
    return columns;
  }

  public String asWikiText() throws Exception {
    StringBuffer wikiText = new StringBuffer();
    if (isLiteralTable)
      wikiText.append("!");
    appendTableWikiText(wikiText);
    return wikiText.toString();
  }

  private void appendTableWikiText(StringBuffer wikiText) throws Exception {
    for (WikiWidget rowWidget : getChildren()) {
      TableRowWidget row = (TableRowWidget) rowWidget;
      wikiText.append("|");
      appendRowWikiText(wikiText, row);
      wikiText.append("\n");
    }
  }

  private void appendRowWikiText(StringBuffer wikiText, TableRowWidget row) throws Exception {
    for (WikiWidget cellWidget : row.getChildren()) {
      TableCellWidget cell = (TableCellWidget) cellWidget;
      appendCellWikiText(wikiText, cell);
      wikiText.append("|");
    }
  }

  private void appendCellWikiText(StringBuffer wikiText, TableCellWidget cell) throws Exception {
    for (WikiWidget contentWidget : cell.getChildren())
      wikiText.append(contentWidget.asWikiText());
  }

  public TableWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      isLiteralTable = "!".equals(match.group(1));
      addRows(text);
      getMaxNumberOfColumns();
    } else
      ; // throw Exception?
  }

  private void getMaxNumberOfColumns() {
    for (WikiWidget widget : children) {
      TableRowWidget rowWidget = (TableRowWidget) widget;
      columns = Math.max(columns, rowWidget.getColumns());
    }
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<table border=\"1\" cellspacing=\"0\">\n");
    html.append(childHtml()).append("</table>\n");

    return html.toString();
  }

  public void addRows(String text) throws Exception {
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      new TableRowWidget(this, match.group(2), isLiteralTable);
      addRows(text.substring(match.end()));
    }
  }

  public void setLiteralTable(boolean isLiteralTable) {
    this.isLiteralTable = isLiteralTable;
  }
}
