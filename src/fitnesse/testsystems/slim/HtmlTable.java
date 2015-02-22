// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import fitnesse.html.HtmlUtil;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.SyntaxError;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;

public class HtmlTable implements Table {
  private static final Logger LOG = Logger.getLogger(HtmlTable.class.getName());

  private static final String SYMBOL_ASSIGNMENT = "\\$[A-Za-z]\\w*<?->?\\[";
  private static final String SYMBOL_ASSIGNMENT_SUFFIX = "\\]";

  // Source: http://dev.w3.org/html5/markup/common-models.html
  private final static Pattern HTML_PATTERN = Pattern.compile("^(?:" + SYMBOL_ASSIGNMENT + ")?<(p|hr|pre|ul|ol|dl|div|h[1-6]|hgroup|address|" +
          "blockquote|ins|del|object|map|video|audio|figure|table|fieldset|canvas|a|em|strong|small|mark|" +
          "abbr|dfn|i|b|s|u|code|var|samp|kbd|sup|sub|q|cite|span|br|ins|del|img|embed|object|video|audio|label|" +
          "output|datalist|progress|command|canvas|time|meter)([ >].*</\\1>|[^>]*/>)" + SYMBOL_ASSIGNMENT_SUFFIX + "?$",
          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern SYMBOL_REPLACEMENT_PATTERN = Pattern.compile("^" + SYMBOL_ASSIGNMENT + ".*" +
          SYMBOL_ASSIGNMENT_SUFFIX + "$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private List<Row> rows = new ArrayList<Row>();
  private TableTag tableNode;

  public HtmlTable(TableTag tableNode) {
    this.tableNode = tableNode;
    NodeList nodeList = tableNode.getChildren();
    for (int i = 0; i < nodeList.size(); i++) {
      Node node = nodeList.elementAt(i);
      if (node instanceof TableRow || node instanceof TableHeader) {
        rows.add(new Row((CompositeTag) node));
      }
    }
  }

  public TableTag getTableNode() {
    return tableNode;
  }

  public String getCellContents(int columnIndex, int rowIndex) {
    return rows.get(rowIndex).getColumn(columnIndex).getContent();
  }

  public int getRowCount() {
    return rows.size();
  }

  public int getColumnCountInRow(int rowIndex) {
    return rows.get(rowIndex).getColumnCount();
  }

  public void substitute(int col, int row, String contents) {
    Cell cell = rows.get(row).getColumn(col);
    cell.setContent(contents);
  }

  public List<List<String>> asList() {
    List<List<String>> list = new ArrayList<List<String>>();
    for (Row row : rows)
      list.add(row.asList());
    return list;
  }

  public String toString() {
    return asList().toString();
  }

  public String toHtml() {
    return tableNode.toHtml();
  }

  public int addRow(List<String> list) {
    Row row = new Row();
    rows.add(row);
    tableNode.getChildren().add(row.getRowNode());
    for (String s : list)
      row.appendCell(s == null ? "" : asHtml(s));
    return rows.size() - 1;
  }

  public void addColumnToRow(int rowIndex, String contents) {
    Row row = rows.get(rowIndex);
    row.appendCell(asHtml(contents));
  }

  /**
   * Scenario tables (mainly) are added on the next row. A bit of javascript allows for collapsing and
   * expanding.
   *
   * @see fitnesse.testsystems.slim.Table#appendChildTable(int, fitnesse.testsystems.slim.Table)
   */
  public void appendChildTable(int rowIndex, Table childTable) {
    Row row = rows.get(rowIndex);
    Row childRow = makeChildRow(row, ((HtmlTable) childTable).getTableNode(), "scenario");
    insertRowAfter(row, childRow);
  }

  private Row makeChildRow(Row row, Node contents, String type) {
    Row childRow = new Row();
    TableColumn column = (TableColumn) newTag(TableColumn.class);
    column.setChildren(new NodeList(contents));
    column.setAttribute("colspan", "" + colspan(row), '"');
    childRow.appendCell(new Cell(column));

    row.rowNode.setAttribute("class", type + " closed", '"');
    childRow.rowNode.setAttribute("class", type + "-detail closed-detail", '"');
    return childRow;
  }

  private int colspan(Row row) {
    NodeList rowNodes = row.rowNode.getChildren();
    int colspan = 0;
    for (int i = 0; i < rowNodes.size(); i++) {
      if (rowNodes.elementAt(i) instanceof TableColumn) {
        String s = ((TableColumn)rowNodes.elementAt(i)).getAttribute("colspan");
        if (s != null) {
          colspan += Integer.parseInt(s);
        } else {
          colspan++;
        }
      }
    }
    return colspan;
  }

  // It's a bit of work to insert a node with the htmlparser module.
  private void insertRowAfter(Row existingRow, Row childRow) {
    NodeList rowNodes = tableNode.getChildren();
    int index = rowNodes.indexOf(existingRow.rowNode);
    Stack<Node> tempStack = new Stack<Node>();

    while (rowNodes.size() - 1 > index) {
      tempStack.push(rowNodes.elementAt(tableNode.getChildren().size() - 1));
      rowNodes.remove(rowNodes.size() - 1);
    }

    rowNodes.add(childRow.rowNode);

    while (!tempStack.isEmpty()) {
      rowNodes.add(tempStack.pop());
    }
  }

  @Override
  public void updateContent(int rowIndex, TestResult testResult) {
    rows.get(rowIndex).setExecutionResult(testResult.getExecutionResult());
  }

  @Override
  public void updateContent(int col, int row, SlimTestResult testResult) {
    Cell cell = rows.get(row).getColumn(col);
    cell.setTestResult(testResult);
    final String newContent = cell.formatTestResult();
    if (newContent != null)
      cell.setContent(newContent);
  }

  @Override
  public void updateContent(int colIndex, int rowIndex, SlimExceptionResult exceptionResult) {
    Row row = rows.get(rowIndex);
    Cell cell = row.getColumn(colIndex);
    if (exceptionResult.hasMessage()) {
      cell.setExceptionResult(exceptionResult);
    } else {
      Row childRow = makeChildRow(row,
              new TextNode("<pre>" + HtmlUtil.escapeHTML(exceptionResult.getException()) + "</pre>"),
              "exception");
      insertRowAfter(row, childRow);
      row.setExecutionResult(exceptionResult.getExecutionResult());
    }
  }

  private static Tag newTag(Class<? extends Tag> klass) {
    Tag tag = null;
    try {
      tag = klass.newInstance();
      tag.setTagName(tag.getTagName().toLowerCase());
      Tag endTag = klass.newInstance();
      endTag.setTagName("/" + tag.getTagName().toLowerCase());
      endTag.setParent(tag);
      tag.setEndTag(endTag);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to create tag from class " + klass, e);
    }
    return tag;
  }


  class Row {
    private List<Cell> cells = new ArrayList<Cell>();
    private CompositeTag rowNode;

    public Row(CompositeTag rowNode) {
      this.rowNode = rowNode;
      NodeList nodeList = rowNode.getChildren();
      for (int i = 0; i < nodeList.size(); i++) {
        Node node = nodeList.elementAt(i);
        if (node instanceof TableColumn)
          cells.add(new Cell((TableColumn) node));
      }
    }

    public Row() {
      rowNode = (TableRow) newTag(TableRow.class);
      rowNode.setChildren(new NodeList());
      Tag endNode = new TableRow();
      endNode.setTagName("/" + rowNode.getTagName().toLowerCase());
      rowNode.setEndTag(endNode);
    }

    public int getColumnCount() {
      return cells.size();
    }

    public Cell getColumn(int columnIndex) {
      return cells.get(columnIndex);
    }

    public void appendCell(String contents) {
      Cell newCell = new Cell(contents);
      appendCell(newCell);
    }

    private void appendCell(Cell newCell) {
      rowNode.getChildren().add(newCell.getColumnNode());
      cells.add(newCell);
    }

    public CompositeTag getRowNode() {
      return rowNode;
    }

    private List<String> asList() {
      List<String> list = new ArrayList<String>();
      for (Cell cell : cells) {
        // was "colorized"
        list.add(cell.getTestResult());
      }
      return list;
    }

    private void setExecutionResult(ExecutionResult executionResult) {
      NodeList cells = rowNode.getChildren();
      for (int i = 0; i < cells.size(); i++) {
        Node cell = cells.elementAt(i);
        if (cell instanceof Tag) {
          Tag tag = (Tag) cell;
          tag.setAttribute("class", executionResult.toString(), '"');
        }
      }
    }
  }

  static class Cell {
    private final TableColumn columnNode;
    private final String originalContent;
    private SlimTestResult testResult;
    private ExceptionResult exceptionResult;

    public Cell(TableColumn tableColumn) {
      columnNode = tableColumn;
      originalContent = columnNode.getChildrenHTML();
    }

    public Cell(String contents) {
      if (contents == null)
        contents = "";
      TextNode text = new TextNode(contents);
      text.setChildren(new NodeList());
      columnNode = (TableColumn) newTag(TableColumn.class);
      columnNode.setChildren(new NodeList(text));
      originalContent = contents;
    }

    public String getContent() {
      String unescaped = columnNode.getChildrenHTML();
      //Some browsers need &nbsp; inside an empty table cell, so we remove it here.
      if ("&nbsp;".equals(unescaped)) {
        return "";
      }
      return qualifiesAsHtml(unescaped) ? unescaped : HtmlUtil.unescapeHTML(unescaped);
    }

    private void setContent(String s) {
      TextNode textNode = new TextNode(s);
      NodeList nodeList = new NodeList(textNode);
      columnNode.setChildren(nodeList);
    }

    public String getTestResult() {
      return testResult != null ? testResult.toString(originalContent) : getContent();
    }

    public TableColumn getColumnNode() {
      return columnNode;
    }

    public void setTestResult(SlimTestResult testResult) {
      this.testResult = testResult;
    }

    public void setExceptionResult(ExceptionResult exceptionResult) {
      if (this.exceptionResult == null) {
        this.exceptionResult = exceptionResult;
        setContent(String.format("%s <span class=\"%s\">%s</span>",
                originalContent,
                exceptionResult.getExecutionResult().toString(),
                asHtml(exceptionResult.getMessage())));
      }
    }

    public String formatTestResult() {
      if (testResult.getExecutionResult() == null) {
        return testResult.getMessage() != null ? asHtml(testResult.getMessage()) : null;
      }
      final String message = testResult.hasMessage()
              ? asHtml(testResult.getMessage())
              : originalContent;
      switch (testResult.getExecutionResult()) {
        case PASS:
          return String.format("<span class=\"pass\">%s</span>", message);
        case FAIL:
          if (testResult.hasActual() && testResult.hasExpected()) {
            return String.format("[%s] <span class=\"fail\">expected [%s]</span>",
                    asHtml(testResult.getActual()),
                    asHtml(testResult.getExpected()));
          } else if ((testResult.hasActual() || testResult.hasExpected()) && testResult.hasMessage()) {
            return String.format("[%s] <span class=\"fail\">%s</span>",
                    asHtml(testResult.hasActual() ? testResult.getActual() : testResult.getExpected()),
                    message);
          }
          return String.format("<span class=\"fail\">%s</span>", message);
        case IGNORE:
          // IGNORE + does not count === Test Not Run
          if (testResult.doesCount()) {
            return String.format("<span class=\"ignore\">%s</span>", message);
          } else {
            return String.format("%s <span class=\"ignore\">%s</span>", originalContent, message);
          }
        case ERROR:
          return String.format("%s <span class=\"error\">%s</span>", originalContent, message);
      }
      return "Should not be here";
    }
  }

  private static String asHtml(String text) {
    if (qualifiesAsHtml(text)) {
      if (qualifiesAsSymbolReplacement(text)) {
        int contentOffset = text.indexOf('[');
        String assignment = text.substring(0, contentOffset);
        String content = text.substring(contentOffset);
        return HtmlUtil.escapeHTML(assignment) + content;
      }
      return text;
    }
    return HtmlUtil.escapeHTML(text);
  }

  @Override
  public HtmlTable asTemplate(CellContentSubstitution substitution) throws SyntaxError {
    String script = this.toHtml();
    // Quick 'n' Dirty
    script = substitution.substitute(script);
    return new HtmlTableScanner(script).getTable(0);
  }

  static boolean qualifiesAsSymbolReplacement(String text) {
    return text.startsWith("$") && SYMBOL_REPLACEMENT_PATTERN.matcher(text).matches();
  }

  static boolean qualifiesAsHtml(String text) {
    // performance improvement: First check 1st character.
    return (text.startsWith("<") || text.startsWith("$")) && HTML_PATTERN.matcher(text).matches();
  }

}


