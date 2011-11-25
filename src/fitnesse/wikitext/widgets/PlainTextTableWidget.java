package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextTableWidget extends ParentWidget implements TableWidget {
  public static final String REGEXP = "^!\\[(?:\\p{Punct}?(?: [^\n]*)?)?\n.+?\n\\]!\n";
  private static final Pattern pattern = Pattern.compile("^!\\[(?:(\\p{Punct}?)(?: ([^\n]*))?)?\n(.+?)\n\\]!", Pattern.DOTALL);
  private String delimiter;
  private String hiddenFirstRow;
  private String body;
  private int columnCount;

  public PlainTextTableWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      delimiter = match.group(1);
      hiddenFirstRow = match.group(2);
      body = match.group(3);

      if (hiddenFirstRow != null && hiddenFirstRow.length() > 0) {
        addHiddenRow(hiddenFirstRow);
      }
      for (String row : body.split("\n")) {
        addRow(row);
      }
    }
  }

  private TableRowWidget addRow(String row) {
    return new TableRowWidget(this, parseToCells(row));
  }

  private String[] parseToCells(String row) {
    if (delimiter.length() == 0)
      return new String[] {row};
    else
      return row.split(delimiter);
  }

  private void addHiddenRow(String rowString) {
    TableRowWidget row = addRow(rowString);
    row.setCommentRow(true);
  }

  public String render() {
    StringBuffer html = new StringBuffer("<table class=\"plain_text_table\">");
    html.append(childHtml()).append("</table>");

    return html.toString();
  }

  public String asWikiText() {
    return "![" + childWikiText() + "]!";
  }

  public WidgetBuilder getBuilder() {
    return WidgetBuilder.literalVariableEvaluatorWidgetBuilder;
  }

  public String getDelimiter() {
    return delimiter == null ? "" : delimiter;
  }

  public String getHiddenFirstRow() {
    return hiddenFirstRow == null ? "" : hiddenFirstRow;
  }

  public String getBody() {
    return body == null ? "" : body;
  }

  public int getColumns() {
    return columnCount;
  }

  public void maximizeColumns(int columns) {
    columnCount = Math.max(columnCount, columns);
  }
}
