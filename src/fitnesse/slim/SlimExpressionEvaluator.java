package fitnesse.slim;

import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.wiki.BaseWikitextPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

import static org.junit.Assert.assertEquals;

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
    Converter<Map> cnv = ConverterRegistry.getConverterForClass(Map.class);

    for (Map.Entry<String, MethodExecutionResult> entry : variables.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue().getObject();
      if(value.toString().startsWith("!{")) {
        value = getHtmlFor(value.toString());
      }
      value = convertWikiHashes(cnv, value);
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

  public Object evaluate(String expression) {
    try {
      return engine.eval(expression);
    } catch (ScriptException e) {
      throw new IllegalArgumentException("Unable to evaluate: " + expression + "; " + e.getMessage(), e);
    }
  }

  private String getHtmlFor(String input) {
    SourcePage page = new DummySourcePage();
    Symbol list = Parser.make(new ParsingPage(page), input).parse();
    return new HtmlTranslator(page, new ParsingPage(page)).translateTree(list);
  }

  private class DummySourcePage implements SourcePage {
    public String content;
    public HashMap<String, String> properties = new HashMap<>();
    public SourcePage includedPage;
    public String targetPath;
    public String url;

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getFullName() {
      return "fullname";
    }

    @Override
    public String getPath() {
      return null;
    }

    @Override
    public String getFullPath() {
      return null;
    }

    @Override
    public String getContent() {
      return content;
    }

    @Override
    public boolean targetExists(String wikiWordPath) {
      return targetPath != null;
    }

    @Override
    public String makeFullPathOfTarget(String wikiWordPath) {
      return targetPath;
    }

    @Override
    public String findParentPath(String targetName) {
      return null;
    }

    @Override
    public Maybe<SourcePage> findIncludedPage(String pageName) {
      return includedPage != null ? new Maybe<>(includedPage) : Maybe.<SourcePage>nothingBecause("missing");
    }

    @Override
    public Collection<SourcePage> getChildren() {
      return null;
    }

    @Override
    public boolean hasProperty(String propertyKey) {
      return properties.containsKey(propertyKey);
    }

    @Override
    public String getProperty(String propertyKey) {
      return properties.containsKey(propertyKey) ? properties.get(propertyKey) : "";
    }

    @Override
    public String makeUrl(String wikiWordPath) {
      return url;
    }

    @Override
    public int compareTo(SourcePage other) {
      return getName().compareTo(other.getName());
    }

    @Override
    public List<Symbol> getSymbols(final SymbolType symbolType) {
      return Collections.emptyList();
    }
  }

}


