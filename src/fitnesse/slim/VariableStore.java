package fitnesse.slim;

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

    if (nameWithDollar.startsWith("$`") && nameWithDollar.endsWith("`")) {
      String expr = nameWithDollar.substring(2, nameWithDollar.length() - 1);
      return evaluate(expr);
    } else {
      String name = nameWithDollar.substring(1);
      if (!variables.containsKey(name)) {
        return null;
      }
      return variables.get(name).getObject();
    }
  }

  private boolean containsValueFor(String nameWithDollar) {
    boolean result = false;
    if (nameWithDollar != null) {
      if (nameWithDollar.startsWith("$`") && nameWithDollar.endsWith("`")) {
        result = true;
      } else if (nameWithDollar.startsWith("$")) {
        if (variables.containsKey(nameWithDollar.substring(1))) {
          result = true;
        }
      }
    }
    return result;
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
    if (symbolName.startsWith("`") && symbolName.endsWith("`")) {
      Object value = getStored("$" + symbolName);
      return String.valueOf(value);
    } else if (variables.containsKey(symbolName)) {
      Object value = variables.get(symbolName);
      return String.valueOf(value);
    }
    return null;
  }

  private Object evaluate(String expr) {
    SlimExpressionEvaluator evaluator = getEvaluatorForExpression(expr);

    Object value = null;
    try {
      value = evaluator.evaluate(expr);
    } catch (IllegalArgumentException e) {
      value = e.getMessage();
    }
    return value;
  }

  protected SlimExpressionEvaluator getEvaluatorForExpression(String expr) {
    SlimExpressionEvaluator evaluator = new SlimExpressionEvaluator();
    evaluator.setContext(expr, variables);
    return evaluator;
  }
}
