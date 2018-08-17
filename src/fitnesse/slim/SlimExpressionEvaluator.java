package fitnesse.slim;

import fitnesse.slim.converters.ConverterRegistry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

/**
 * Evaluates slim expressions.
 */
public class SlimExpressionEvaluator {
  private static final ScriptEngineManager ENGINE_MANAGER = new ScriptEngineManager();

  private ScriptEngine engine;

  public SlimExpressionEvaluator() {
    this("JavaScript");
  }

  public SlimExpressionEvaluator(String engineName) {
    this(ENGINE_MANAGER.getEngineByName(engineName));
  }

  public SlimExpressionEvaluator(ScriptEngine engine) {
    this.engine = engine;
  }

  public void setContext(Map<String, MethodExecutionResult> variables) {
    Converter<Map> mapCnv = ConverterRegistry.getConverterForClass(Map.class);
    Converter<List> listCnv = ConverterRegistry.getConverterForClass(List.class);

    for (Map.Entry<String, MethodExecutionResult> entry : variables.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue().getObject();
      if(null != value) {
        value = convertWikiHashes(mapCnv, value);
        value = convertWikiLists(listCnv, value);
      }
      engine.put(key, value);
    }
  }

  private Object convertWikiHashes(Converter<Map> cnv, Object value) {
    value = convertWikiHash(cnv, value);
    if (value instanceof Map) {
      value = convertNestedWikiHashes(cnv, (Map<String, ?>) value);
    }
    return value;
  }

  private Object convertWikiLists(Converter<List> cnv, Object value) {
    if (value.toString().startsWith("[") && value.toString().endsWith("]")) {
      value = convertWikiList(cnv, value);
    }
    return value;
  }

  private Object convertWikiHash(Converter<Map> cnv, Object value) {
    if (value instanceof String) {
      Map mapObj = cnv.fromString((String) value);
      if (!mapObj.isEmpty()) {
        value = mapObj;
      }
    }
    return value;
  }

  private Object convertNestedWikiHashes(Converter<Map> cnv, Map<String, ?> value) {
    Map<String, Object> newValue = new LinkedHashMap<>();
    newValue.putAll(value);
    for (Map.Entry<String, Object> nestedEntry : newValue.entrySet()) {
      Object nestedValue = convertWikiHashes(cnv, nestedEntry.getValue());
      newValue.put(nestedEntry.getKey(), nestedValue);
    }
    value = newValue;
    return value;
  }

  private Object convertWikiList(Converter<List> cnv, Object value) {
    if (value instanceof String) {
      List listObj = cnv.fromString((String) value);
      if (!listObj.isEmpty()) {
        value = listObj;
      }
    }
    return value;
  }

  public Object evaluate(String expression) {
    try {
      return engine.eval(expression);
    } catch (ScriptException e) {
      throw new IllegalArgumentException("Unable to evaluate: " + expression + "; " + e.getMessage(), e);
    }
  }


}


