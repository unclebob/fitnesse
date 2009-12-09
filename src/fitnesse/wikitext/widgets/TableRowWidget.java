// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableRowWidget extends ParentWidget {
  private static final Pattern pattern = Pattern.compile("\\|([^\\|\n\r]*)");
  private TableWidget parentTable;

  private boolean isLiteral;
  private boolean isCommentRow = false;

  public TableRowWidget(StandardTableWidget parentTable, String text, boolean isLiteral) throws Exception {
    super(parentTable);
    this.parentTable = parentTable;
    this.isLiteral = isLiteral;
    addCells(text);
  }

  public TableRowWidget(PlainTextTableWidget tableWidget, String[] cells) throws Exception {
    super(tableWidget);
    this.parentTable = tableWidget;
    tableWidget.maximizeColumns(cells.length);
    for (String cell : cells) {
      new TableCellWidget(this, cell, true);
    }
  }

  public int getColumns() {
    return numberOfChildren();
  }

  public TableWidget getParentTable() {
    return parentTable;
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer(getRowStartTag());
    html.append(childHtml()).append("</tr>\n");
    return html.toString();
  }

  private String getRowStartTag() {
    if (isCommentRow) {
      return "<tr class=\"hidden\">";
    } else {
      return "<tr>";
    }
  }
  
  public void addCells(String text) throws Exception {
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      new TableCellWidget(this, match.group(1), isLiteral);
      addCells(text.substring(match.end()));
    }
  }

  public void markAsCommentRow() {
    isCommentRow  = true;
  }

  public void setCommentRow(boolean b) {
    isCommentRow = b;
  }
}

