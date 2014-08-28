package fitnesse.slim;


import static util.ListUtility.list;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
      List<Object> list = list();
      // Make the test stable by ordering the keys
      TreeSet<String> orderedKeySet = new TreeSet<String>(theMap.keySet());
      for (String key : orderedKeySet) {
        list.add(list(key, theMap.get(key)));
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
}
