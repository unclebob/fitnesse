// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.WidgetBuilder;

public class TableCellWidget extends ParentWidget {
  @SuppressWarnings("unused")
  private static Pattern NEWLINE_PATTERN = Pattern.compile("\\\\(" + LINE_BREAK_PATTERN + ")");

  private TableRowWidget parentRow = null;
  private boolean isLiteral;

  public TableCellWidget(TableRowWidget parentRow, String text, boolean isLiteral) throws Exception {
    super(parentRow);
    this.parentRow = parentRow;
    this.isLiteral = isLiteral;
    addChildWidgets(groomText(text));
  }

  private String groomText(String text) {
    text = text.replaceAll("\\\\\\r", "\r").replaceAll("\\\\\\n", "\n");
    return text.trim();
  }

  public String render() throws Exception {
    return makeCellTag();
  }

  private String makeCellTag() throws Exception {
    HtmlTag cellTag = new HtmlTag("td");
    if (computeColSpan().length() > 0) {
      cellTag.addAttribute("colspan", computeColSpan());
    }
    if (childHtml().equals(""))
      cellTag.add("&nbsp;"); // Some browsers don't like empty table cells.
    else
      cellTag.add(childHtml());
    return cellTag.html();
  }

  private String computeColSpan() {
    int currentColumn = parentRow.children.indexOf(this) + 1;
    int maxTableColumn = parentRow.getParentTable().getColumns();
    int maxColumnThisRow = parentRow.numberOfChildren();

    String colspan = "";
    if (currentColumn == maxColumnThisRow && currentColumn != maxTableColumn) {
      colspan = String.valueOf(maxTableColumn - currentColumn + 1);
    }
    return colspan;
  }

  public WidgetBuilder getBuilder() {
    if (isLiteral)
      return WidgetBuilder.literalVariableEvaluatorWidgetBuilder;
    else
      return parent.getBuilder();
  }
}

