package fitnesse.responders.run.slimResponder;

import fitnesse.wikitext.Utils;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.*;
import org.htmlparser.util.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTable implements Table {
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

  public String literalize(String s) {
    return s;
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

  private Tag newTag(Class klass) {
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

  class Row {
    private List<Cell> cells = new ArrayList<Cell>();
    private CompositeTag rowNode;
    private Pattern coloredCellPattern = Pattern.compile("<span class=\"(\\w*)\">(.*)(</span>)");

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
      rowNode.getChildren().add(newCell.getColumnNode());
      cells.add(newCell);
    }

    public CompositeTag getRowNode() {
      return rowNode;
    }

    public List<String> asList() {
      List<String> list = new ArrayList<String>();
      for (Cell cell : cells) {
        String content = cell.getContent();
        content = colorize(content);
        list.add(content);
      }
      return list;
    }

    // This terrible algorithm is an example of either my hatred, or my ignorance, of regular expressions.
    private String colorize(String content) {
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


