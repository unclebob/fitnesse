package fitnesse.slim.converters;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.slim.Converter;
import fitnesse.testsystems.slim.HtmlTable;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.util.HashMap;
import java.util.Map;

public class MapConverter implements Converter<Map> {

  private NodeList nodes;

  private NodeList tables;

  @Override
  public String toString(Map hash) {
    if (hash == null) {
      return NULL_VALUE;
    }

    HtmlTag table = createTag(hash);

    return table.html().trim();
  }

  protected HtmlTag createTag(Map<?, ?> hash) {
    // Use HtmlTag, same as we do for fitnesse.wikitext.parser.HashTable.
    HtmlTag table = new HtmlTag("table");
    table.addAttribute("class", "hash_table");
    for (Map.Entry<?, ?> entry : hash.entrySet()) {
      HtmlTag row = new HtmlTag("tr");
      row.addAttribute("class", "hash_row");
      table.add(row);

      HtmlTag keyCell = new HtmlTag("td");
      addCellContent(keyCell, entry.getKey());
      keyCell.addAttribute("class", "hash_key");
      row.add(keyCell);

      HtmlTag valueCell = new HtmlTag("td");
      addCellContent(valueCell, entry.getValue());
      valueCell.addAttribute("class", "hash_value");
      row.add(valueCell);
    }
    return table;
  }

  protected void addCellContent(HtmlTag valueCell, Object cellValue) {
    String valueToAdd = ElementConverterHelper.elementToString(cellValue);
    if (!HtmlUtil.isValidTableCellContent(valueToAdd) && !HtmlTable.qualifiesAsConvertedList(valueToAdd))
      valueToAdd = HtmlUtil.escapeHTML(valueToAdd);

    valueCell.add(valueToAdd.trim());
  }

  @Override
  public Map<String, String> fromString(String possibleTable) {
    Map<String, String> map = new HashMap<>();
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
    return HtmlUtil.unescapeHTML(((CompositeTag) compositeNode).getChildrenHTML());
  }

  private NodeList parseHtml(String possibleTable) {
      try {
        Parser parser = Parser.createParser(possibleTable, null);
        return parser.parse(null);
      } catch (Exception e) {
        return null;
      }
  }

}
