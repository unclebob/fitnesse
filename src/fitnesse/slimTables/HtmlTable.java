// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.slim.SlimError;
import fitnesse.wikitext.Utils;

public class HtmlTable implements Table {
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

  public String getCellContents(int columnIndex, int rowIndex) {
    return rows.get(rowIndex).getColumn(columnIndex).getContent();
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

  public void appendCellToRow(int rowIndex, Table table) {
    Row row = rows.get(rowIndex);
    row.appendCell(table);
  }

  public void setTestStatusOnRow(int rowIndex, boolean testStatus) {
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
      tag = (Tag) klass.newInstance();
      Tag endTag = (Tag) klass.newInstance();
      endTag.setTagName("/" + tag.getTagName());
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
      endNode.setTagName("/" + rowNode.getTagName());
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

    public void appendCell(Table table) {
      try {
        doAppendCollapsableSection((HtmlTable) table);
      } catch (Exception e) {
        throw new SlimError(e);
      }

    }

    private void doAppendCollapsableSection(HtmlTable htmlTable) throws Exception {
      Node collapsableDiv = getCollapsableDiv();
      NodeList children = collapsableDiv.getChildren();
      Node hiddenDiv = findHiddenDiv(children);
      hiddenDiv.setChildren(new NodeList(htmlTable.getTableNode()));
      Cell newCell = new Cell(collapsableDiv);
      appendCell(newCell);
    }

    private Node findHiddenDiv(NodeList children) {
      Node hiddenDiv = null;
      for (int i = 0; i < children.size(); i++) {
        Node n = children.elementAt(i);
        if (n instanceof Div) {
          Div div = (Div) n;
          if ("hidden".equals(div.getAttribute("class"))) {
            hiddenDiv = n;
            break;
          }
        }
      }
      return hiddenDiv;
    }

    private Node getCollapsableDiv() throws Exception {
      String collapsableSectionHtml = makeCollapsableSection();
      Parser parser = new Parser(collapsableSectionHtml);
      NodeList htmlTree = parser.parse(null);
      Node collapsableDiv = htmlTree.elementAt(0);
      return collapsableDiv;
    }

    public String makeCollapsableSection() throws Exception {
      String id = new Random().nextLong() + "";
      HtmlTag outerDiv;

      outerDiv = HtmlUtil.makeDivTag("collapse_rim");

      HtmlTag image = new HtmlTag("img");
      image.addAttribute("src", "/files/images/collapsableClosed.gif");
      image.addAttribute("class", "left");
      image.addAttribute("id", "img" + id);

      HtmlTag anchor = new HtmlTag("a", image);
      anchor.addAttribute("href", "javascript:toggleCollapsable('" + id + "');");

      outerDiv.add(anchor);
      HtmlTag span = new HtmlTag("span", "Scenario");
      span.addAttribute("id", "test_status");
      outerDiv.add(span);
      HtmlTag collapsablediv = HtmlUtil.makeDivTag("hidden");
      collapsablediv.addAttribute("id", id);
      collapsablediv.add("");
      outerDiv.add(collapsablediv);

      return outerDiv.html();
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

    public void setTestStatus(boolean testStatus) {
      NodeList cells = rowNode.getChildren();
      Node lastCell = cells.elementAt(cells.size() - 1);
      Tag statusNode = findById(lastCell, "test_status");
      statusNode.setAttribute("class", testStatus ? "pass" : "fail");
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


