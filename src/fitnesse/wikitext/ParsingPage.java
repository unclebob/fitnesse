package fitnesse.wikitext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The page represents wiki page in the course of being parsed.
 */
public class ParsingPage implements VariableStore {

  private final SourcePage page;
  private final SourcePage namedPage; // included page
  private final VariableSource variableSource;
  private final Cache cache;

  public ParsingPage(SourcePage page) {
    this(page, new Cache());
  }

  private ParsingPage(SourcePage page, Cache cache) {
    this(page, page, cache, cache);
  }

  public ParsingPage(SourcePage page, VariableSource variableSource, Cache cache) {
    this(page, page, variableSource, cache);
  }

  private ParsingPage(SourcePage page, SourcePage namedPage, VariableSource variableSource, Cache cache) {
    this.page = page;
    this.namedPage = namedPage;
    this.variableSource = new CompositeVariableSource(
            new NamedPageVariableSource(namedPage),
            variableSource);
    this.cache = cache;
  }

  public ParsingPage copyForNamedPage(SourcePage namedPage) {
    return new ParsingPage(
            this.page,
            namedPage,
            this.variableSource,
            this.cache);
  }

  public SourcePage getPage() {
    return page;
  }

  public SourcePage getNamedPage() {
    return namedPage;
  }

  public int nextId() { return cache.nextId(); }

  @Override
  public void putVariable(String name, String value) {
    cache.putVariable(name, value);
  }

  @Override
  public Optional<String> findVariable(String name) {
    return variableSource != null ? variableSource.findVariable(name) : Optional.empty();
  }

  public static class Cache implements VariableStore {

    private final Map<String, String> cache;
    private int id;

    public Cache() {
      this(new HashMap<>());
    }

    public Cache(Map<String, String> cache) {
      this.cache = cache;
    }

    @Override
    public void putVariable(String name, String value) {
      cache.put(name, value);
    }

    @Override
    public Optional<String> findVariable(String name) {
      return Optional.ofNullable(cache.get(name));
    }

    public int nextId() {
      return id++;
    }
  }


  private static class NamedPageVariableSource implements VariableSource {

    private final SourcePage namedPage;

    private NamedPageVariableSource(SourcePage namedPage) {
      this.namedPage = namedPage;
    }

    @Override
    public Optional<String> findVariable(String key) {
      String value;
      if (key.equals("PAGE_NAME"))
        value = namedPage.getName();
      else if (key.equals("PAGE_PATH"))
        value = namedPage.getPath();
      else
        return Optional.empty();

      return Optional.ofNullable(value);
    }
  }
}
