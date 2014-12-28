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
  private final ParentPageVariableSource parentPageVariableSource;

  public ParsingPage(SourcePage page) {
    this(page, null);
  }

  public ParsingPage(SourcePage page, VariableSource variableSource) {
    this(page, page, variableSource, new Cache());
  }

  private ParsingPage(SourcePage page, SourcePage namedPage, VariableSource variableSource, Cache cache) {
    this.page = page;
    this.namedPage = namedPage;
    this.variableSource = variableSource != null ? variableSource : new NothingVariableSource();
    this.cache = cache;
    
    SourcePage parentPage = page.getParent();
    this.parentPageVariableSource = parentPage != null ? new ParentPageVariableSource(parentPage, variableSource) : null;
  }

  public ParsingPage copyForNamedPage(SourcePage namedPage) {
    return new ParsingPage(this.page, namedPage, variableSource, cache);
  }

  public SourcePage getPage() {
    return page;
  }

  public SourcePage getNamedPage() {
    return namedPage;
  }

  public void putVariable(String name, String value) {
    cache.putVariable(name, new Maybe<String>(value));
  }

  @Override
  public Maybe<String> findVariable(String name) {
    Maybe<String> result = new PageVariableSource(page, namedPage).findVariable(name);
    if (!result.isNothing()) return result;

    result = new ApplicationVariableSource(variableSource).findVariable(name);
    if (!result.isNothing()) return result;

    result = new UserVariableSource(variableSource).findVariable(name);
    if (!result.isNothing()) return result;

    result = cache.findVariable(name);
    if (!result.isNothing()) return result;

    if (parentPageVariableSource != null) {
      result = parentPageVariableSource.findVariable(name);
      if (!result.isNothing()) return result;
    }

    return variableSource.findVariable(name);
  }


  public static class UserVariableSource implements VariableSource {

    private final VariableSource variableSource;

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


  public static class Cache implements VariableSource {

    private final Map<String, Maybe<String>> cache;

    public Cache() {
      this(new HashMap<String, Maybe<String>>());
    }

    public Cache(Map<String, Maybe<String>> cache) {
      this.cache = cache;
    }

    public void putVariable(String name, Maybe<String> value) {
      cache.put(name, value);
    }

    @Override
    public Maybe<String> findVariable(String name) {
      if (!cache.containsKey(name)) return Maybe.noString;
      return cache.get(name);
    }
  }


  public static class PageVariableSource implements VariableSource {

    private final SourcePage page;
    private final SourcePage namedPage;

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


  public static class ParentPageVariableSource implements VariableSource {

    private final ParsingPage page;
    private boolean parsed = false;

    public ParentPageVariableSource(SourcePage page, VariableSource variableSource) {
      this.page = new ParsingPage(page, variableSource);
    }

    @Override
    public Maybe<String> findVariable(String name) {
      if (!parsed) {
        // The cache is passed along... page is rendered as a normal page.
        parsed = true;
        Parser.make(page, page.page.getContent()).parse();
      }
      return page.findVariable(name);
    }

  }

  public static class ApplicationVariableSource implements VariableSource {

    private final VariableSource variableSource;

    public ApplicationVariableSource(VariableSource variableSource) {
      this.variableSource = variableSource;
    }

    @Override
    public Maybe<String> findVariable(String name) {
      String value;
      if (name.equals("FITNESSE_PORT")) {
        Maybe<String> port = variableSource.findVariable("FITNESSE_PORT");
        value = port.isNothing() ? "-1" : port.getValue();
      } else if (name.equals("FITNESSE_ROOTPATH")) {
        Maybe<String> path = variableSource.findVariable("FITNESSE_ROOTPATH");
        value = path.isNothing() ? "" : path.getValue();
      } else if (name.equals("FITNESSE_VERSION")) {
        Maybe<String> version = variableSource.findVariable("FITNESSE_VERSION");
        value = version.isNothing() ? "" : version.getValue();
      } else {
        return Maybe.noString;
      }
      return new Maybe<String>(value);
    }
  }


  private static class CompositeVariableSource implements VariableSource {

    private final VariableSource[] variableSources;

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

  public static class NothingVariableSource implements VariableSource {

    @Override
    public Maybe<String> findVariable(String name) {
      return Maybe.noString;
    }
  }
}
