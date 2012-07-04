// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;

import fitnesse.responders.run.ExecutionResult;
import fitnesse.wikitext.Utils;

public class HtmlTable implements Table {
  private static final Random RANDOM_GENERATOR = new SecureRandom();
  private static Pattern coloredCellPattern = Pattern.compile("<span class=\"(\\w*)\">(.*)(</span>)");
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

  public void setName(String tableName) {
    tableNode.setAttribute("table_name", tableName);
  }

  public String getCellContents(int columnIndex, int rowIndex) {
    return rows.get(rowIndex).getColumn(columnIndex).getContent();
  }

  public String getCellResult(int col, int row) {
    return rows.get(row).getColumn(col).getResult();
  }


  public String getUnescapedCellContents(int col, int row) {
    return Utils.unescapeHTML(getCellContents(col, row));
  }

  public void appendToCell(int col, int row, String message) {
    Cell cell = rows.get(row).getColumn(col);
    cell.setContent(cell.getEscapedContent() + message);
  }

  public int getRowCount() {
    return rows.size();
  }

  public int getColumnCountInRow(int rowIndex) {
    return rows.get(rowIndex).getColumnCount();
  }

  public void setCell(int col, int row, String contents) {
    rows.get(row).getColumn(col).setContent(contents);
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

  public int addRow(List<String> list) throws Exception {
    Row row = new Row();
    rows.add(row);
    tableNode.getChildren().add(row.getRowNode());
    for (String s : list)
      row.appendCell(s == null ? "" : s);
    return rows.size() - 1;
  }

  public void appendCellToRow(int rowIndex, String contents) throws Exception {
    Row row = rows.get(rowIndex);
    row.appendCell(contents);
  }

  /**
   * Scenario tables (mainly) are added on the next row. A bit of javascript allows for collapsing and
   * expanding.
   * 
   * @see fitnesse.slimTables.Table#appendChildTable(int, fitnesse.slimTables.Table)
   */
  public void appendChildTable(int rowIndex, Table childTable) {
    Row row = rows.get(rowIndex);
    row.rowNode.setAttribute("class", "scenario closed", '"');

    Row childRow = new Row();
    TableColumn column = (TableColumn) newTag(TableColumn.class);
    column.setChildren(new NodeList(((HtmlTable) childTable).getTableNode()));
    column.setAttribute("colspan", "" + colspan(row), '"');
    childRow.appendCell(new Cell(column));
    childRow.rowNode.setAttribute("class", "scenario-detail", '"');
    insertRowAfter(row, childRow);
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
    
    while (tempStack.size() > 0) {
      rowNodes.add(tempStack.pop());
    }
  }

  
  public void setTestStatusOnRow(int rowIndex, ExecutionResult testStatus) {
    Row row = rows.get(rowIndex);
    row.setTestStatus(testStatus);
  }

  public String error(String s) {
    return String.format("<span class=\"error\">%s</span>", s);
  }

  public String pass(String s) {
    return String.format("<span class=\"pass\">%s</span>", s);
  }

  public String fail(String s) {
    return String.format("<span class=\"fail\">%s</span>", s);
  }

  public String ignore(String s) {
    return String.format("<span class=\"ignore\">%s</span>", s);
  }

  private Tag newTag(Class<? extends Tag> klass) {
    Tag tag = null;
    try {
      tag = klass.newInstance();
      tag.setTagName(tag.getTagName().toLowerCase());
      Tag endTag = klass.newInstance();
      endTag.setTagName("/" + tag.getTagName().toLowerCase());
      endTag.setParent(tag);
      tag.setEndTag(endTag);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tag;
  }


  // This terrible algorithm is an example of either my hatred, or my ignorance, of regular expressions.
  public static String colorize(String content) {
    while (true) {
      int firstMatchEnd = content.indexOf("</span>");
      if (firstMatchEnd != -1) {
        firstMatchEnd += "</span>".length();
        Matcher matcher = coloredCellPattern.matcher(content);
        matcher.region(0, firstMatchEnd);
        if (matcher.find()) {
          String color = matcher.group(1);
          String coloredString = matcher.group(2);
          content = content.replace(matcher.group(), String.format("%s(%s)", color, coloredString));
        } else
          break;
      } else {
        break;
      }
    }
    return content;
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

    public List<String> asList() {
      List<String> list = new ArrayList<String>();
      for (Cell cell : cells) {
        list.add(colorize(cell.getContent()));
      }
      return list;
    }

    private void setTestStatus(ExecutionResult testStatus) {
      NodeList cells = rowNode.getChildren();
      for (int i = 0; i < cells.size(); i++) {
        Node cell = cells.elementAt(i);
        if (cell instanceof Tag) {
          Tag tag = (Tag) cell;
          tag.setAttribute("class", String.format("\"%s\"", testStatus.toString()));
        }
      }
    }

    private Tag findById(Node node, String id) {
      if (hasId(node, id))
        return (Tag) node;
      return findChildMatchingId(node, id);
    }

    private Tag findChildMatchingId(Node node, String id) {
      NodeList children = node.getChildren();
      if (children != null) {
        for (int i = 0; i < children.size(); i++) {
          Node child = children.elementAt(i);
          Tag found = findById(child, id);
          if (found != null)
            return found;
        }
      }
      return null;
    }

    private boolean hasId(Node node, String id) {
      if (node instanceof Tag) {
        Tag t = (Tag) node;
        if (id.equals(t.getAttribute("id")))
          return true;
      }
      return false;
    }
  }

  class Cell {
    private TableColumn columnNode;

    public Cell(TableColumn tableColumn) {
      columnNode = tableColumn;
    }

    public Cell(String contents) {
      if (contents == null)
        contents = "";
      TextNode text = new TextNode(contents);
      text.setChildren(new NodeList());
      columnNode = (TableColumn) newTag(TableColumn.class);
      columnNode.setChildren(new NodeList(text));
    }

    public Cell(Node node) {
      columnNode = (TableColumn) newTag(TableColumn.class);
      columnNode.setChildren(new NodeList(node));
    }

    public String getResult() {
      String result = columnNode.getAttribute("class");
      if (result == null) {
        Node child = columnNode.getFirstChild();
        if (child != null)
        return child.getText();
      } else if (result.equals("pass") || result.equals("fail") || result.equals("error") || result.equals("ignore")) {
        return result;
      }
      return "Unkown Result";
    }

    public String getContent() {
      return getEscapedContent();
    }

    public String getEscapedContent() {
      String unescaped = columnNode.getChildrenHTML();
      //Some browsers need &nbsp; inside an empty table cell, so we remove it here.
      return "&nbsp;".equals(unescaped) ? "" : unescaped;
    }

    public void setContent(String s) {
      TextNode textNode = new TextNode(s);
      NodeList nodeList = new NodeList(textNode);
      columnNode.setChildren(nodeList);
    }

    public TableColumn getColumnNode() {
      return columnNode;
    }
  }
}


 