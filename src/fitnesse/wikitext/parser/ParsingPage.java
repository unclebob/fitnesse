package fitnesse.wikitext.parser;

import java.util.HashMap;
import java.util.Map;

import fitnesse.wiki.UrlPathVariableSource;

/**
 * The page represents wiki page in the course of being parsed.
 */
public class ParsingPage implements VariableSource {

  private final SourcePage page;
  private final SourcePage namedPage;
  private final VariableSource variableSource;

  private final Cache cache;

  public ParsingPage(SourcePage page) {
    this(page, null);
  }

  public ParsingPage(SourcePage page, VariableSource variableSource) {
    this(page, page, variableSource, new Cache());
  }

  public ParsingPage copyForNamedPage(SourcePage namedPage) {
    return new ParsingPage(this.page, namedPage, variableSource, cache);
  }

  private ParsingPage(SourcePage page, SourcePage namedPage, VariableSource variableSource, Cache cache) {
    this.page = page;
    this.namedPage = namedPage;
    this.variableSource = variableSource;
    this.cache = cache;
  }

  public SourcePage getPage() {
    return page;
  }

  public SourcePage getNamedPage() {
    return namedPage;
  }

  public void putVariable(String name, String value) {
    cache.putVariable(page, name, new Maybe<String>(value));
  }

  @Override
  public Maybe<String> findVariable(String name) {
    Maybe<String> result = new PageVariableSource(page, namedPage).findVariable(name);
    if (!result.isNothing()) return result;

    result = new ApplicationVariableSource(variableSource).findVariable(name);
    if (!result.isNothing()) return result;

    result = new UserVariableSource(variableSource).findVariable(name);
    if (!result.isNothing()) return result;

    result = new ParentPageVariableSource(page, cache).findVariable(name);
    if (!result.isNothing()) return result;

    return variableSource != null ? variableSource.findVariable(name) : Maybe.noString;
  }


  public static class UserVariableSource implements VariableSource {

    private VariableSource variableSource;

    public UserVariableSource(VariableSource variableSource) {
      this.variableSource = variableSource;
    }

    @Override
    public Maybe<String> findVariable(String name) {
      if(variableSource instanceof UrlPathVariableSource){
        Maybe<String> result = ((UrlPathVariableSource) variableSource).findUrlVariable(name);
        if (!result.isNothing()) return result;
      }
      return Maybe.noString;
    }
  }


  public static class Cache {

    private Map<String, Map<String, Maybe<String>>> cache;

    public Cache() {
      this(new HashMap<String, Map<String, Maybe<String>>>());
    }

    public Cache(Map<String, Map<String, Maybe<String>>> cache) {
      this.cache = cache;
    }

    public void putVariable(SourcePage page, String name, Maybe<String> value) {
      String key = page.getFullName();
      if (!cache.containsKey(key)) cache.put(key, new HashMap<String, Maybe<String>>());
      cache.get(key).put(name, value);
    }

    public Maybe<String> findVariable(SourcePage page, String name) {
      String key = page.getFullName();
      if (!cache.containsKey(key)) return Maybe.noString;
      if (!cache.get(key).containsKey(name)) return Maybe.noString;
      return cache.get(key).get(name);
    }

    private boolean inCache(SourcePage page) {
      return cache.containsKey(page.getFullName());
    }
  }


  public static class PageVariableSource implements VariableSource {

    private SourcePage page;
    private SourcePage namedPage;

    public PageVariableSource(SourcePage page, SourcePage namedPage) {
      this.page = page;
      this.namedPage = namedPage;
    }

    public Maybe<String> findVariable(String key) {
      String value;
      if (key.equals("RUNNING_PAGE_NAME"))
        value = page.getName();
      else if (key.equals("RUNNING_PAGE_PATH"))
        value = page.getPath();
      else if (key.equals("PAGE_NAME"))
        value = namedPage.getName();
      else if (key.equals("PAGE_PATH"))
        value = namedPage.getPath();
      else
        return Maybe.noString;

      return new Maybe<String>(value);
    }
  }


  public class ParentPageVariableSource implements VariableSource {

    private final SourcePage page;
    private final Cache cache;

    public ParentPageVariableSource(SourcePage page, Cache cache) {
      this.page = page;
      this.cache = cache;
    }

    private ParsingPage copyForPage(SourcePage page) {
      return new ParsingPage(page, page, variableSource, cache);
    }

    @Override
    public Maybe<String> findVariable(String name) {
      Maybe<String> localVariable = cache.findVariable(page, name);
      if (!localVariable.isNothing()) return localVariable;
      return lookInParentPages(name);
    }

    private Maybe<String> lookInParentPages(String name) {
      for (SourcePage sourcePage : page.getAncestors()) {
        if (!cache.inCache(sourcePage)) {
          // The cache is passed along... page is rendered as a normal page.
          Parser.make(copyForPage(sourcePage), sourcePage.getContent()).parse();
          cache.putVariable(sourcePage, "", Maybe.noString);
        }
        Maybe<String> result = cache.findVariable(sourcePage, name);
        if (!result.isNothing()) return result;
      }
      return Maybe.noString;
    }

  }

  public static class ApplicationVariableSource implements VariableSource {

    private VariableSource variableSource;

    public ApplicationVariableSource(VariableSource variableSource) {
      this.variableSource = variableSource;
    }

    @Override
    public Maybe<String> findVariable(String name) {
      String value;
      if (name.equals("FITNESSE_PORT")) {
        Maybe<String> port = findVariableInContext("FITNESSE_PORT");
        value = port.isNothing() ? "-1" : port.getValue();
      } else if (name.equals("FITNESSE_ROOTPATH")) {
        Maybe<String> path = findVariableInContext("FITNESSE_ROOTPATH");
        value = path.isNothing() ? "" : path.getValue();
      } else if (name.equals("FITNESSE_VERSION")) {
        Maybe<String> version = findVariableInContext("FITNESSE_VERSION");
        value = version.isNothing() ? "" : version.getValue();
      } else {
        return Maybe.noString;
      }
      return new Maybe<String>(value);
    }
    private Maybe<String> findVariableInContext(String name) {
      return variableSource != null ? variableSource.findVariable(name) : Maybe.noString;
    }
  }


  private static class CompositeVariableSource implements VariableSource {

    private VariableSource[] variableSources;

    public CompositeVariableSource(VariableSource... variableSources) {
      this.variableSources = variableSources;
    }

    @Override
    public Maybe<String> findVariable(String name) {
      for (VariableSource variableSource : variableSources) {
        Maybe<String> result = variableSource.findVariable(name);
        if (!result.isNothing()) return result;
      }
      return Maybe.noString;
    }
  }
}
