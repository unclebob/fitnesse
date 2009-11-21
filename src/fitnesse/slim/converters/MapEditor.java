package fitnesse.slim.converters;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;

public class MapEditor extends PropertyEditorSupport {
  private NodeList nodes;
  private NodeList tables;

  public MapEditor() {
  }

  public String toString(Object o) {
    return "TILT";
  }

  @Override
  public void setAsText(String s) throws IllegalArgumentException {
    setValue(fromString(s));
  }

  public Object fromString(String possibleTable) {
    HashMap<String, String> map = new HashMap<String, String>();
    if (tableIsValid(possibleTable))
      extractRowsIntoMap(map, tables);

    return map;
  }

  private boolean tableIsValid(String possibleTable) {
    if (isValidHtml(possibleTable)) {
      return hasOneTable();
    } else {
      return false;
    }
  }

  private boolean hasOneTable() {
    TagNameFilter tableFilter = new TagNameFilter("table");
    tables = nodes.extractAllNodesThatMatch(tableFilter);
    return tables.size() == 1;
  }

  private boolean isValidHtml(String possibleTable) {
    nodes = parseHtml(possibleTable);
    return nodes != null;
  }

  private void extractRowsIntoMap(HashMap<String, String> map, NodeList tables) {
    extractRows(map, getRows(tables));
  }

  private void extractRows(HashMap<String, String> map, NodeList rows) {
    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      extractRow(map, rows, rowIndex);
    }
  }

  private void extractRow(HashMap<String, String> map, NodeList rows, int rowIndex) {
    Node row = rows.elementAt(rowIndex);
    if (row != null)
      extractColumns(map, row);
  }

  private void extractColumns(HashMap<String, String> map, Node row) {
    TagNameFilter tdFilter = new TagNameFilter("td");
    if (row.getChildren() != null) {
      NodeList cols = row.getChildren().extractAllNodesThatMatch(tdFilter);
      if (cols.size() == 2)
        addColsToMap(map, cols);
    }
  }

  private void addColsToMap(HashMap<String, String> map, NodeList cols) {
    String key = getText(cols.elementAt(0));
    String value = getText(cols.elementAt(1));
    map.put(key, value);
  }

  private NodeList getRows(NodeList tables) {
    TagNameFilter trFilter = new TagNameFilter("tr");
    Node table = tables.elementAt(0);

    if (table.getChildren() != null)
      return table.getChildren().extractAllNodesThatMatch(trFilter);

    return new NodeList();
  }

  private String getText(Node compositeNode) {
    return ((CompositeTag) compositeNode).getChildrenHTML();
  }

  private NodeList parseHtml(String possibleTable) {
    try {
      Parser parser = new Parser(possibleTable);
      return parser.parse(null);
    } catch (ParserException e) {
      return null;
    }
  }
}
