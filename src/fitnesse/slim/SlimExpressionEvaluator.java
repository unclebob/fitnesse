package fitnesse.slim;

import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.GenericCollectionConverter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  public void setContext(String expr, Map<String, MethodExecutionResult> variables) {
    Converter<Map> mapCnv = ConverterRegistry.getConverterForClass(Map.class);
    Converter<List> listCnv = ConverterRegistry.getConverterForClass(List.class);

    for (Map.Entry<String, MethodExecutionResult> entry : variables.entrySet()) {
      String key = entry.getKey();
      if (expr.contains(key)) {
        Object value = entry.getValue().getObject();
        value = convertWikiHashes(mapCnv, value);
        value = convertWikiLists(listCnv, value);
        engine.put(key, value);
      }
    }
  }

  protected Object convertWikiHashes(Converter<Map> cnv, Object value) {
    value = convertWikiHash(cnv, value);
    if (value instanceof Map) {
      value = convertNestedWikiHashes(cnv, (Map<String, ?>) value);
    }
    return value;
  }

  protected Object convertWikiLists(Converter<List> cnv, Object value) {
    boolean mightBeList = value instanceof String;
    if (mightBeList) {
      String v = (String) value;
      if (GenericCollectionConverter.class.equals(cnv.getClass())) {
        // generic converter also create (single item) list when no brackets are
        // present, that's not what we want here
        mightBeList = v.startsWith("[") && v.endsWith("]");
      }
      if (mightBeList) {
        value = convertWikiList(cnv, v);
      }
    }
    return value;
  }

  protected Object convertWikiHash(Converter<Map> cnv, Object value) {
    if (value instanceof String) {
      Map mapObj = cnv.fromString((String) value);
      if (!mapObj.isEmpty()) {
        value = mapObj;
      }
    }
    return value;
  }

  protected Object convertNestedWikiHashes(Converter<Map> cnv, Map<String, ?> value) {
    Map<String, Object> newValue = new LinkedHashMap<>();
    newValue.putAll(value);
    for (Map.Entry<String, Object> nestedEntry : newValue.entrySet()) {
      Object nestedValue = convertWikiHashes(cnv, nestedEntry.getValue());
      newValue.put(nestedEntry.getKey(), nestedValue);
    }
    value = newValue;
    return value;
  }

  protected Object convertWikiList(Converter<List> cnv, String value) {
    return cnv.fromString(value);
  }

  public Object evaluate(String expression) {
    try {
      return engine.eval(expression);
    } catch (ScriptException e) {
      throw new IllegalArgumentException("Unable to evaluate: " + expression + "; " + e.getMessage(), e);
    }
  }


}


