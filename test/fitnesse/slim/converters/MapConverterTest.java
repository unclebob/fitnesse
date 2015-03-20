package fitnesse.slim.converters;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import fitnesse.html.HtmlTag;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapConverterTest {
  private MapConverter converter;
  private Map<String, String> result;

  @Before
  public void setup() {

    converter = new MapConverter();
  }

  @Test
  public void fromEmptyString_shouldCreateEmptyMap() throws Exception {
    makeMap("");
    assertEquals(0, result.size());
  }

  @SuppressWarnings("unchecked")
  private void makeMap(String inputString) {
    result = converter.fromString(inputString);
  }

  @Test
  public void fromGarbageString_shouldCreateEmptyMap() throws Exception {
    makeMap("sdfwewdfsdfwefsdfsdfswe");
    assertEquals(0, result.size());
  }

  @Test
  public void fromEmptyTable_shouldCreateEmptyMap() throws Exception {
    makeMap("<table></table>");
    assertEquals(0, result.size());
  }

  @Test
  public void fromTableWithNoColumns_shouldCreateEmptyMap() throws Exception {
    makeMap("<table><tr></tr><tr></tr></table>");
    assertEquals(0, result.size());
  }

  @Test
  public void fromTableWithOneColumn_shouldCreateEmptyMap() throws Exception {
    makeMap("<table><tr><td>0</td></tr></table>");
    assertEquals(0, result.size());
  }

  @Test
  public void fromTableWithMoreThanTwoColumns_shouldCreateEmptyMap() throws Exception {
    makeMap("<table>" + "<tr>" + "  <td>0</td>" + "  <td>0</td>" + "  <td>0</td>" + "</tr>" + "</table>");
    assertEquals(0, result.size());
  }

  @Test
  public void fromTableWithTwoColumnsAndOneRow_shouldCreateMapWithOneEntry() throws Exception {
    makeMap("<table>" + "<tr>" + "  <td>name</td>" + "  <td>Bob</td>" + "</tr>" + "</table>");
    assertEquals(1, result.size());
    assertEquals("Bob", result.get("name"));
  }

  @Test
  public void fromTableWithTwoColumnsAndTwoRows_shouldCreateMapWithTwoEntries() throws Exception {
    makeMap("<table>" + "<tr>" + "  <td>name</td>" + "  <td>Bob</td>" + "</tr>" + "<tr>" + "  <td>address</td>" + "  <td>here</td>" + "</tr>" + "</table>");
    assertEquals(2, result.size());
    assertEquals("Bob", result.get("name"));
    assertEquals("here", result.get("address"));
  }

  @Test
  public void fromTwoValidTables_shouldCreateEmptyMap() throws Exception {
    makeMap("<table>" + "<tr>" + "  <td>name</td>" + "  <td>Bob</td>" + "</tr>" + "</table>" + "<table>" + "<tr>" + "  <td>name</td>" + "  <td>Bob</td>" + "</tr>" + "</table>");
    assertEquals(0, result.size());
  }

  @Test
  public void shouldRenderTableAsHtml() {
    MapConverter editor = new MapConverter();

    assertEquals(StringUtils.join(Arrays.asList("<table class=\"hash_table\">", "\t<tr class=\"hash_row\">", "\t\t<td class=\"hash_key\">a</td>", "\t\t<td class=\"hash_value\">b</td>", "\t</tr>",
        "\t<tr class=\"hash_row\">", "\t\t<td class=\"hash_key\">c</td>", "\t\t<td class=\"hash_value\">d</td>", "\t</tr>", "</table>"), HtmlTag.endl), editor.toString(aMap()));
  }

  private Map<Object, Object> aMap() {
    Map<Object, Object> map = new TreeMap<Object, Object>();
    map.put("a", "b");
    map.put("c", "d");
    return map;
  }
}
