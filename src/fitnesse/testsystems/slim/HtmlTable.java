// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTable implements Table {
  private static final Logger LOG = Logger.getLogger(HtmlTable.class.getName());

  private static final String SYMBOL_ASSIGNMENT = "\\$[A-Za-z]\\w*<?->?\\[|\\$`[^`]+`<?->?\\[";
  private static final String SYMBOL_ASSIGNMENT_SUFFIX = "\\]";

  private static final Pattern HTML_PATTERN = Pattern.compile("^(?:" + SYMBOL_ASSIGNMENT + ")?" +
                                                                HtmlUtil.HTML_CELL_CONTENT_PATTERN_TEXT +
                                                                SYMBOL_ASSIGNMENT_SUFFIX + "?$",
                                                          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern CONTAINS_TABLE_HTML_PATTERN = Pattern.compile(".*(<table.*?>.*</table>).*",
                                                                          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern SYMBOL_REPLACEMENT_PATTERN = Pattern.compile("^(" + SYMBOL_ASSIGNMENT + ")(.*)(" +
          SYMBOL_ASSIGNMENT_SUFFIX + ")$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private List<Row> rows = new ArrayList<>();
  private TableTag tableNode;
  private boolean isTearDown;

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

  @Override
  public boolean isTearDown() {
    return isTearDown;
  }

  public void setTearDown(boolean teardown) {
    isTearDown = teardown;
  }

  @Override
  public String getCellContents(int columnIndex, int rowIndex) {
    return rows.get(rowIndex).getColumn(columnIndex).getContent();
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public int getColumnCountInRow(int rowIndex) {
    return rows.get(rowIndex).getColumnCount();
  }

  @Override
  public void substitute(int col, int row, String contents) {
    Cell cell = rows.get(row).getColumn(col);
    cell.setContent(contents);
  }

  public List<List<String>> asList() {
    List<List<String>> list = new ArrayList<>();
    for (Row row : rows)
      list.add(row.asList());
    return list;
  }

  @Override
  public String toString() {
    return asList().toString();
  }

  public String toHtml() {
    return tableNode.toHtml();
  }

  @Override
  public int addRow(List<String> list) {
    Row row = new Row();
    rows.add(row);
    tableNode.getChildren().add(row.getRowNode());
    for (String s : list)
      row.appendCell(s == null ? "" : asHtml(s));
    return rows.size() - 1;
  }

  @Override
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
  @Override
  public void appendChildTable(int rowIndex, Table childTable) {
    Row row = rows.get(rowIndex);
    Row childRow = makeChildRow(row, ((HtmlTable) childTable).getTableNode(), "scenario");
    insertRowAfter(row, childRow);
  }

  private void addChildTableInCell(int colIndex, int rowIndex, String childText) {
    Row row = rows.get(rowIndex);

    Cell cell = row.getColumn(colIndex);
    String originalContent = cell.getContent();
    cell.addChildTable(originalContent, "error", childText);
  }

  private void insertRow(int rowIndex, String childText,
      String rowType, ExecutionResult executionResult) {
    Row row = rows.get(rowIndex);
    Row childRow = makeChildRow(row,
        new TextNode("<pre>" + HtmlUtil.escapeHTML(childText) + "</pre>"),
        rowType);
    insertRowAfter(row, childRow);
    row.setExecutionResult(executionResult);
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
    Stack<Node> tempStack = new Stack<>();

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
    if (colIndex < 0) { // Row Exception
      insertRow(rowIndex, exceptionResult.getException(), "exception",
          exceptionResult.getExecutionResult());
    } else {
      Cell cell = row.getColumn(colIndex);
      cell.setExceptionResult(exceptionResult);
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
    private List<Cell> cells = new ArrayList<>();
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
      List<String> list = new ArrayList<>();
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

    public void addChildTable(String headerText, String classType,
        String childText) {
      String childTable = "<table><tr class=\"exception closed\"><td> <span class=\""
          + classType + "\">" + headerText
          + "</span></td></tr><tr class=\"exception-detail closed-detail\"><td>"
          + childText + "</td</tr></table>";
      this.setContent(childTable);
    }

    public void setTestResult(SlimTestResult testResult) {
      this.testResult = testResult;
    }

    public void setExceptionResult(SlimExceptionResult exceptionResult) {
      if (this.exceptionResult == null) {
        if(exceptionResult.getException().contains("IGNORE_")){
          setIgnoreResult(exceptionResult);
          return;
        }
        this.exceptionResult = exceptionResult;
        String exceptionText = exceptionResult.getMessage();
        if (exceptionText != null) {
          setContent(String.format("%s <span class=\"%s\">%s</span>",
              originalContent, exceptionResult.getExecutionResult().toString(),
              asHtml(exceptionText)));
        } else {
          exceptionText = exceptionResult.getException();
          addChildTable(
              originalContent,
              exceptionResult.getExecutionResult().toString(),
              asHtml(exceptionText)
          );
        }
      }
    }

    public void setIgnoreResult(SlimExceptionResult exceptionResult) {
      if (this.exceptionResult == null) {
        this.exceptionResult = exceptionResult;
        String exceptionText = exceptionResult.getException();
        if (exceptionText != null) {
          setContent(String.format("%s <span class=\"%s\">%s</span>",
            originalContent, exceptionResult.getIgnoreExecutionResult().toString(),
            asHtml(exceptionText)));
        }
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
          if (testResult.hasExpected()) {
            if (qualifiesAsHtml(testResult.getActual()) || qualifiesAsHtml(testResult.getExpected())) {
              return String.format("<span class=\"pass\">%s</span>",
                asHtml(testResult.getExpected()));
            } else {
              String[] expected = parseSymbol(testResult.getExpected());
              return String.format("<span class=\"pass\">%s</span>",
                HtmlUtil.escapeHTML(expected[0]) +
                  HtmlUtil.escapeHTML(expected[1]) +
                  HtmlUtil.escapeHTML(expected[2]));
            }
          } else {
            return String.format("<span class=\"pass\">%s</span>", message);
          }
        case FAIL:
          if (testResult.hasActual() && testResult.hasExpected()) {
            if (qualifiesAsHtml(testResult.getActual()) || qualifiesAsHtml(testResult.getExpected())) {
              return String.format("[%s] <span class=\"fail\">expected [%s]</span>",
                  asHtml(testResult.getActual()),
                  asHtml(testResult.getExpected()));
            } else {
              String[] actual = parseSymbol(testResult.getActual());
              String[] expected = parseSymbol(testResult.getExpected());
              return String.format("[%s] <span class=\"fail\">expected [%s]</span>",
                HtmlUtil.escapeHTML(actual[0]) +
                  HtmlDiffUtil.buildActual(actual[1], expected[1]) +
                  HtmlUtil.escapeHTML(actual[2]),
                HtmlUtil.escapeHTML(expected[0]) +
                  new HtmlDiffUtil.ExpectedBuilder(testResult.getActual(), expected[1])
                    .setOpeningTag("<span class=\"diff\">")
                    .setClosingTag("</span>").build() +
                  HtmlUtil.escapeHTML(expected[2]));
            }
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
    if (containsHtmlTable(text) && qualifiesAsConvertedList(text)) {
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

  public static boolean qualifiesAsConvertedList(String text) {
    if (qualifiesAsSymbolReplacement(text)) {
      int contentOffset = text.indexOf('[');
      String symbolContent = text.substring(contentOffset + 1, text.length() -1);
      return qualifiesAsConvertedList(symbolContent);
    }
    return text.startsWith("[") && text.endsWith("]");
  }

  static boolean containsHtmlTable(String text) {
    return text.contains("<") && CONTAINS_TABLE_HTML_PATTERN.matcher(text).matches();
  }

  private static String[] parseSymbol(String text) {
    Matcher matcher = SYMBOL_REPLACEMENT_PATTERN.matcher(text);
    String[] symbols = new String[] {"","",""};
    if (matcher.matches()) {
      symbols[0] = matcher.group(1);
      symbols[1] = matcher.group(2);
      symbols[2] = matcher.group(3);
    } else {
      symbols[1] = text;
    }
    return symbols;
  }

  static boolean qualifiesAsHtml(String text) {
    // performance improvement: First check 1st character.
    return (text.startsWith("<") || text.startsWith("$")) && HTML_PATTERN.matcher(text).matches();
  }

}
