package fitnesse.slim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableStore {
  public static final Pattern SYMBOL_PATTERN = Pattern.compile("\\$([A-Za-z]\\w*)");
  private Map<String, MethodExecutionResult> variables = new HashMap<String, MethodExecutionResult>();
  private Matcher symbolMatcher;

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
    List<Object> result = new ArrayList<Object>();
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
    int startingPosition = 0;
    while (true) {
      if ("".equals(arg) || null == arg) {
        break;
      }
      symbolMatcher = SYMBOL_PATTERN.matcher(arg);
      if (symbolMatcher.find(startingPosition)) {
        String symbolName = symbolMatcher.group(1);
        arg = replaceSymbolInArg(arg, symbolName);
        startingPosition = symbolMatcher.start(1);
      } else {
        break;
      }
    }
    return arg;
  }

  private String replaceSymbolInArg(String arg, String symbolName) {
    if (variables.containsKey(symbolName)) {
      String replacement = "null";
      Object value = variables.get(symbolName);
      if (value != null) {
        replacement = value.toString();
      }
      arg = arg.substring(0, symbolMatcher.start()) + replacement
          + arg.substring(symbolMatcher.end());
    }
    return arg;
  }

}
