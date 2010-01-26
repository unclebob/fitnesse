// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardTableWidget extends ParentWidget implements TableWidget {
  public static final String REGEXP = "^-?!?(?:\\|[^\n]*?\\|\n)+";
  public static final Pattern pattern = Pattern.compile("(-?)(!?)(\\|[^\n]*?)\\|\n");

  public boolean isLiteralTable;
  public boolean isCommentTable;
  private int columns = 0;

  public int getColumns() {
    return columns;
  }

  public String asWikiText() throws Exception {
    StringBuffer wikiText = new StringBuffer();
    if (isCommentTable) {
      wikiText.append("-");
    }
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

  public StandardTableWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      isCommentTable = "-".equals(match.group(1));
      isLiteralTable = "!".equals(match.group(2));
      addRows(text, isCommentTable);
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

  private void addRows(String text, boolean markAsCommentRow) throws Exception {
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      addRow(match.group(3), markAsCommentRow);
      addRows(text.substring(match.end()), false);
    }
  }

  private void addRow(String wikiTextRow, boolean markAsCommentRow) throws Exception {
    TableRowWidget rowWidget = new TableRowWidget(this, wikiTextRow, isLiteralTable);
    if (markAsCommentRow) {
      rowWidget.markAsCommentRow();
    }
  }
  

  public void setLiteralTable(boolean isLiteralTable) {
    this.isLiteralTable = isLiteralTable;
  }
}
