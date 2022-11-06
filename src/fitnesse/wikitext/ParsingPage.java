package fitnesse.wikitext;

import fitnesse.wiki.ApplicationVariableSource;
import fitnesse.wiki.PageVariableSource;
import fitnesse.wiki.UrlPathVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wiki.WikitextPage;
import fitnesse.wikitext.parser.Maybe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  private int id;

  public ParsingPage(SourcePage page) {
    this(page, new Cache());
  }

  public ParsingPage(WikiPage page, VariableSource variables) {
    SourcePage source = new WikiSourcePage(page);
    this.cache = new Cache();
    this.variableSource = new CompositeVariableSource(
      new NamedPageVariableSource(source),
      new ApplicationVariableSource(variables),
      new PageVariableSource(page),
      new UserVariableSource(variables),
      cache,
      new ParentPageVariableSource(page),
      variables);
    this.page = source;
    this.namedPage = source;
  }

  public ParsingPage(SourcePage source, VariableSource variables) {
    this.cache = new Cache();
    this.variableSource = new CompositeVariableSource(
      new NamedPageVariableSource(source),
      cache,
      variables);
    this.page = source;
    this.namedPage = source;
  }

  private ParsingPage(SourcePage page, Cache cache) {
    this(page, page, cache, cache);
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

  public List<String> listVariables() {
    return cache.listVariables();
  }

  @Override
  public int nextId() { return id++; }

  @Override
  public void putVariable(String name, String value) {
    cache.putVariable(name, value);
  }

  @Override
  public Optional<String> findVariable(String name) {
    return variableSource != null ? variableSource.findVariable(name) : Optional.empty();
  }

  private static class Cache implements VariableSource {

    private final Map<String, String> cache = new HashMap<>();

    public Cache() {}

    @Override
    public Optional<String> findVariable(String name) {
      return Optional.ofNullable(cache.get(name));
    }

    public void putVariable(String name, String value) {
      cache.put(name, value);
    }

    public List<String> listVariables(){
      return new ArrayList<>(cache.keySet());
    }
  }

  private static class UserVariableSource implements VariableSource {

    private final VariableSource variableSource;

    public UserVariableSource(VariableSource variableSource) {
      this.variableSource = variableSource;
    }

    @Override
    public Optional<String> findVariable(String name) {
      if(variableSource instanceof UrlPathVariableSource){
        Maybe<String> result = ((UrlPathVariableSource) variableSource).findUrlVariable(name);
        if (!result.isNothing()) return Optional.of(result.getValue());
      }
      return Optional.empty();
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

  public static class ParentPageVariableSource implements VariableSource {
    private final WikiPage page;

    public ParentPageVariableSource(WikiPage page) {

      this.page = page;
    }

    @Override
    public Optional<String> findVariable(String name) {
      if (page.isRoot()) {
        return Optional.empty();
      }
      WikiPage parentPage = page.getParent();
      if (parentPage instanceof WikitextPage) {
        return ((WikitextPage) parentPage).getSyntaxTree().findVariable(name);
      } else {
        return Optional.ofNullable(parentPage.getVariable(name));
      }
    }
  }
}
