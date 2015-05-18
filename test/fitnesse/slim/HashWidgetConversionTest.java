package fitnesse.slim;


import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HashWidgetConversionTest extends HashWidgetConversionTestBase {

  public static class MapConstructor extends MapReceptor {
    public MapConstructor(Map<String, String> map) {
      theMap = map;
    }
    
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
      List<Object> list = new ArrayList<Object>();
      // Make the test stable by ordering the keys
      TreeSet<String> orderedKeySet = new TreeSet<String>(theMap.keySet());
      for (String key : orderedKeySet) {
        list.add(Arrays.asList(key, theMap.get(key)));
      }
      return list;
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
  public void fromTableWithNestedTables_shouldCreateMapWithThreeEntries() throws Exception {
    String instance1Id = "a";
    statementExecutor.create(instance1Id, NestedMapSender.class.getName());

    String expected =
            "<table class=\"hash_table\">\n" +
                    "\t<tr class=\"hash_row\">\n" +
                    "\t\t<td class=\"hash_key\">address</td>\n" +
                    "\t\t<td class=\"hash_value\">1</td>\n" +
                    "\t</tr>\n" +
                    "\t<tr class=\"hash_row\">\n" +
                    "\t\t<td class=\"hash_key\">nestedMap</td>\n" +
                    "\t\t<td class=\"hash_value\">\n" +
                    "\t\t\t<table class=\"hash_table\">\n" +
                    "\t\t\t\t<tr class=\"hash_row\">\n" +
                    "\t\t\t\t\t<td class=\"hash_key\">name2</td>\n" +
                    "\t\t\t\t\t<td class=\"hash_value\">Bob2</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\t\t\t\t<tr class=\"hash_row\">\n" +
                    "\t\t\t\t\t<td class=\"hash_key\">address2</td>\n" +
                    "\t\t\t\t\t<td class=\"hash_value\">2</td>\n" +
                    "\t\t\t\t</tr>\n" +
                    "\t\t\t</table>\n" +
                    "\t\t</td>\n" +
                    "\t</tr>\n" +
                    "\t<tr class=\"hash_row\">\n" +
                    "\t\t<td class=\"hash_key\">name</td>\n" +
                    "\t\t<td class=\"hash_value\">Bob</td>\n" +
                    "\t</tr>\n" +
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
    return ((String) resp).replace("\r\n", "\n");
  }

  public static class NestedMapSender {
    public LinkedHashMap<String, Object> theMap = new LinkedHashMap<String, Object>();

    public NestedMapSender() {
      theMap.put("address", 1);
      Map<String, Object> nestedMap = new LinkedHashMap<String, Object>();
      nestedMap.put("name2", "Bob2");
      nestedMap.put("address2", 2);
      theMap.put("nestedMap", nestedMap);
      theMap.put("name", "Bob");
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
