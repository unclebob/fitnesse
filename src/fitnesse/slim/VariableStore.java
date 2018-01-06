package fitnesse.slim;

import fitnesse.slim.converters.MapConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableStore {

  private Map<String, MethodExecutionResult> variables = new HashMap<>();

  public void setSymbol(String name, MethodExecutionResult value) {
    variables.put(name, value);
  }

  public MethodExecutionResult getSymbol(String name) {
    return variables.get(name);
  }

  public Object getStored(String nameWithDollar) {
    if (nameWithDollar == null || !nameWithDollar.startsWith("$"))
      return null;

    String name = nameWithDollar.substring(1);
    if (!variables.containsKey(name)) {
      return null;
    }
    return variables.get(name).getObject();
  }

  private boolean containsValueFor(String nameWithDollar) {
    return nameWithDollar != null && nameWithDollar.startsWith("$")
      && variables.containsKey(nameWithDollar.substring(1));
  }

  public Object[] replaceSymbols(Object[] args) {
    Object[] result = new Object[args.length];
    for (int i = 0; i < args.length; i++)
      result[i] = replaceSymbol(args[i]);

    return result;
  }

  private List<Object> replaceSymbolsInList(List<Object> objects) {
    List<Object> result = new ArrayList<>();
    for (Object object : objects)
      result.add(replaceSymbol(object));

    return result;
  }

  @SuppressWarnings("unchecked")
  private Object replaceSymbol(Object object) {
    if (object instanceof List) {
      return replaceSymbolsInList((List<Object>) object);
    }
    if (containsValueFor((String) object)) {
      return getStored((String) object);
    }
    return replaceSymbolsInString((String) object);
  }

  public String replaceSymbolsInString(String arg) {
    // Symbol assignments are not done by the fixture code so remove them and
    // return an empty string
    if (SlimSymbol.isSymbolAssignment(arg) != null)
      return "";

    return new SlimSymbol() {

      @Override
      protected String getSymbolValue(String symbolName) {
        return getStoreSymbolValue(symbolName);
      }

    }.replace(arg);

  }

  private String getStoreSymbolValue(String symbolName) {
    Object value = null;
    if (symbolName.startsWith("`") && symbolName.endsWith("`")) {
      String expr = symbolName.substring(1, symbolName.length() - 1);
      try {
        value = getDotValue(expr);
      } catch (IllegalArgumentException e) {
        // ignore just leave value null
      }
    } else if (variables.containsKey(symbolName)) {
      value = variables.get(symbolName);
    }
    return String.valueOf(value);
  }

  private Object getDotValue(String expr) {
    Object value = null;
    String[] symbolInfo = expr.split("\\.");
    expr = "$" + symbolInfo[0];
    if (containsValueFor(expr)) {
      value = getStored(expr);
      for (int i = 1; i < symbolInfo.length; i++) {
        String key = symbolInfo[i];
        if (value instanceof Map) {
          value = getValueFromMap((Map<String, ?>) value, key);
        } else if (value == null) {
          break;
        } else {
          value = getValueFromMap(value.toString(), key);
        }
      }
    }
    return value;
  }

  private Object getValueFromMap(String map, String key) {
    MapConverter cnv = new MapConverter();
    Map<String, String> mapObj = cnv.fromString(map);
    return getValueFromMap(mapObj, key);
  }

  private Object getValueFromMap(Map<String, ?> map, String key) {
    if (!map.containsKey(key)) {
      throw new IllegalArgumentException("No key: " + key);
    }
    return map.get(key);
  }
}
