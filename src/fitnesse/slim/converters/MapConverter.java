package fitnesse.slim.converters;

import java.util.HashMap;
import java.util.Map;

import fitnesse.html.HtmlTag;
import fitnesse.slim.Converter;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class MapConverter implements Converter<Map> {

  private static final String[] specialHtmlChars = new String[] { "&", "<", ">" };
  private static final String[] specialHtmlEscapes = new String[] { "&amp;", "&lt;", "&gt;" };

  private NodeList nodes;

  private NodeList tables;

  @Override
  public String toString(Map hash) {
    // Use HtmlTag, same as we do for fitnesse.wikitext.parser.HashTable.
    HtmlTag table = new HtmlTag("table");
    table.addAttribute("class", "hash_table");
    for (Map.Entry<?, ?> entry : ((Map<?, ?>) hash).entrySet()) {
      HtmlTag row = new HtmlTag("tr");
      row.addAttribute("class", "hash_row");
      table.add(row);
      String key = entry.getKey().toString();
      HtmlTag keyCell = new HtmlTag("td", key.trim());
      keyCell.addAttribute("class", "hash_key");
      row.add(keyCell);

      String value = entry.getValue().toString();
      HtmlTag valueCell = new HtmlTag("td", value.trim());
      valueCell.addAttribute("class", "hash_value");
      row.add(valueCell);
    }
    return table.html().trim();
  }

  @Override
  public Map<String, String> fromString(String possibleTable) {
    Map<String, String> map = new HashMap<String, String>();
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

  private void extractRowsIntoMap(Map<String, String> map, NodeList tables) {
    extractRows(map, getRows(tables));
  }

  private void extractRows(Map<String, String> map, NodeList rows) {
    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      extractRow(map, rows, rowIndex);
    }
  }

  private void extractRow(Map<String, String> map, NodeList rows, int rowIndex) {
    Node row = rows.elementAt(rowIndex);
    if (row != null)
      extractColumns(map, row);
  }

  private void extractColumns(Map<String, String> map, Node row) {
    TagNameFilter tdFilter = new TagNameFilter("td");
    if (row.getChildren() != null) {
      NodeList cols = row.getChildren().extractAllNodesThatMatch(tdFilter);
      if (cols.size() == 2)
        addColsToMap(map, cols);
    }
  }

  private void addColsToMap(Map<String, String> map, NodeList cols) {
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

  public String escapeHTML(String value) {
    return replaceStrings(value, specialHtmlChars, specialHtmlEscapes);
  }

  private String replaceStrings(String value, String[] originalStrings, String[] replacementStrings) {
    String result = value;
    for (int i = 0; i < originalStrings.length; i++)
      if (result.contains(originalStrings[i]))
        result = result.replace(originalStrings[i], replacementStrings[i]);
    return result;
  }

}
