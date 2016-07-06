package fitnesse.slim;


import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class HashWidgetConversionTest extends HashWidgetConversionTestBase {

  public static class MapConstructor extends MapReceptor {
    public MapConstructor(Map<String, String> map) {
      theMap = map;
    }

    @Override
    public boolean setMap(Map<String, String> map) {
      return false;
    }
  }

  public static class MapReceptor {
    public Map<String, String> theMap;

    public boolean setMap(Map<String, String> map) {
      theMap = map;
      return true;
    }

    public List<Object> query() {
      return queryAsArrayList();
    }

    public ArrayList<Object> queryAsArrayList() {
      ArrayList<Object> list = new ArrayList<>();
      // Make the test stable by ordering the keys
      TreeSet<String> orderedKeySet = new TreeSet<>(theMap.keySet());
      for (String key : orderedKeySet) {
        list.add(Arrays.asList(key, theMap.get(key)));
      }
      return list;
    }

    public Object queryAsObject() {
      return queryAsArrayList();
    }
  }

  @Override
  protected StatementExecutorInterface createStatementExecutor() {
    return new StatementExecutor();
  }

  @Override
  protected String mapReceptorClassName() {
    return MapReceptor.class.getName();
  }

  @Override
  protected String mapConstructorClassName() {
    return MapConstructor.class.getName();
  }

  @Test
  public void methodsReturningExactlyListShouldNotBeConverteredToStrings() throws Exception {
    String instance1Id = "a";
    statementExecutor.create(instance1Id, MapReceptor.class.getName());

    assertEquals("true", statementExecutor.call(instance1Id, "setMap",
                    "<table>" +
                    "<tr>" +
                    "  <td>name</td>" +
                    "  <td>Bob</td>" +
                    "</tr>" +
                    "</table>"));

    Object respQuery = statementExecutor.call(instance1Id, "query");
    assertNotNull(respQuery);
    assertTrue(respQuery instanceof List);

    Object respObject = statementExecutor.call(instance1Id, "queryAsObject");
    String actualQueryObj = checkStringResponse("queryAsObject()", respObject);
    assertEquals(respQuery.toString(), actualQueryObj);

    Object respArrayList = statementExecutor.call(instance1Id, "queryAsArrayList");
    String actualArrayList = checkStringResponse("queryAsArrayList()", respArrayList);
    assertEquals(respQuery.toString(), actualArrayList);
  }

  @Test
  public void fromTableWithNestedTables_shouldCreateMapWithThreeEntries() throws Exception {
    String instance1Id = "a";
    statementExecutor.create(instance1Id, NestedMapSender.class.getName());

    String expected =
            "<table class=\"hash_table\">" +
                    "<tr class=\"hash_row\">" +
                    "<td class=\"hash_key\">address</td>" +
                    "<td class=\"hash_value\">1</td>" +
                    "</tr>" +
                    "<tr class=\"hash_row\">" +
                    "<td class=\"hash_key\">nestedMap</td>" +
                    "<td class=\"hash_value\">" +
                    "<table class=\"hash_table\">" +
                    "<tr class=\"hash_row\">" +
                    "<td class=\"hash_key\">name2</td>" +
                    "<td class=\"hash_value\">Bob2</td>" +
                    "</tr>" +
                    "<tr class=\"hash_row\">" +
                    "<td class=\"hash_key\">address2</td>" +
                    "<td class=\"hash_value\">2</td>" +
                    "</tr>" +
                    "</table>" +
                    "</td>" +
                    "</tr>" +
                    "<tr class=\"hash_row\">" +
                    "<td class=\"hash_key\">name</td>" +
                    "<td class=\"hash_value\">Bob</td>" +
                    "</tr>" +
                    "<tr class=\"hash_row\"><td class=\"hash_key\">list</td><td class=\"hash_value\">[a, b]</td></tr>" +
                    "<tr class=\"hash_row\"><td class=\"hash_key\">nullKey</td><td class=\"hash_value\">null</td></tr>" +
                    "</table>";

    Object respMap = statementExecutor.call(instance1Id, "getMap");
    String actualMap = checkStringResponse("getMap()", respMap);

    Object respObject = statementExecutor.call(instance1Id, "getMapAsObject");
    String actualObj = checkStringResponse("getMapAsObject()", respObject);

    Object respLinked = statementExecutor.call(instance1Id, "getLinkedMap");
    String actualLinked = checkStringResponse("getLinkedMap()", respLinked);

    assertEquals(actualMap, actualLinked);
    assertEquals(actualMap, actualObj);
    assertEquals(expected, actualMap);
  }

  private String checkStringResponse(String method, Object resp) {
    assertTrue("Other object than String result from " + method
                + ": " + resp.getClass().getName(), resp instanceof String);
    return ((String) resp).replace("\r", "").replace("\n", "").replace("\t", "");
  }

  public static class NestedMapSender {
    public LinkedHashMap<String, Object> theMap = new LinkedHashMap<>();

    public NestedMapSender() {
      theMap.put("address", 1);
      Map<String, Object> nestedMap = new LinkedHashMap<>();
      nestedMap.put("name2", "Bob2");
      nestedMap.put("address2", 2);
      theMap.put("nestedMap", nestedMap);
      theMap.put("name", "Bob");
      theMap.put("list", Arrays.asList("a", "b"));
      theMap.put("nullKey", null);
    }

    public Map<String, Object> getMap() {
      return theMap;
    }
    public Object getMapAsObject() {
      return theMap;
    }

    public LinkedHashMap<String, Object> getLinkedMap() {
      return theMap;
    }
  }
}
