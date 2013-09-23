package fitnesse.slim.converters;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class MapConverterTest {
  private MapEditor converter;
  private Map<String, String> result;

  @Before
  public void setup() {

    converter = new MapEditor();
  }

  @Test
  public void fromEmptyString_shouldCreateEmptyMap() throws Exception {
    makeMap("");
    assertEquals(0, result.size());
  }

  @SuppressWarnings("unchecked")
  private void makeMap(String inputString) {
    result = (Map<String, String>) converter.fromString(inputString);
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
    makeMap(
      "<table>" +
        "<tr>" +
        "  <td>0</td>" +
        "  <td>0</td>" +
        "  <td>0</td>" +
        "</tr>" +
        "</table>");
    assertEquals(0, result.size());
  }

  @Test
  public void fromTableWithTwoColumnsAndOneRow_shouldCreateMapWithOneEntry() throws Exception {
    makeMap(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>");
    assertEquals(1, result.size());
    assertEquals("Bob", result.get("name"));
  }

  @Test
  public void fromTableWithTwoColumnsAndTwoRows_shouldCreateMapWithTwoEntries() throws Exception {
    makeMap(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "<tr>" +
        "  <td>address</td>" +
        "  <td>here</td>" +
        "</tr>" +
        "</table>");
    assertEquals(2, result.size());
    assertEquals("Bob", result.get("name"));
    assertEquals("here", result.get("address"));
  }

  @Test
  public void fromTwoValidTables_shouldCreateEmptyMap() throws Exception {
    makeMap(
      "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>" +
        "<table>" +
        "<tr>" +
        "  <td>name</td>" +
        "  <td>Bob</td>" +
        "</tr>" +
        "</table>");
    assertEquals(0, result.size());
  }
}
