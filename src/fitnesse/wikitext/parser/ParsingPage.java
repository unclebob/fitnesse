package fitnesse.wikitext.parser;

import java.util.HashMap;

import util.Maybe;

/**
 * The page represents wiki page in the course of being parsed.
 */
public class ParsingPage implements VariableSource {

  private final SourcePage page;
  private final SourcePage namedPage;
  private final VariableSource variableSource;

  private final HashMap<String, HashMap<String, Maybe<String>>> cache;

  public ParsingPage(SourcePage page) {
    this(page, null);
  }

  public ParsingPage(SourcePage page, VariableSource variableSource) {
    this(page, page, variableSource, new HashMap<String, HashMap<String, Maybe<String>>>());
  }

  public ParsingPage copy() {
    return new ParsingPage(page, page, variableSource, cache);
  }

  public ParsingPage copyForPage(SourcePage page) {
    return new ParsingPage(page, page, variableSource, cache);
  }

  public ParsingPage copyForNamedPage(SourcePage namedPage) {
    return new ParsingPage(this.page, namedPage, variableSource, cache);
  }

  private ParsingPage(SourcePage page, SourcePage namedPage, VariableSource variableSource, HashMap<String, HashMap<String, Maybe<String>>> cache) {
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

  private boolean inCache(SourcePage page) {
    return cache.containsKey(page.getFullName());
  }

  private Maybe<String> findVariableInCache(SourcePage page, String name) {
    String key = page.getFullName();
    if (!cache.containsKey(key)) return Maybe.noString;
    if (!cache.get(key).containsKey(name)) return Maybe.noString;
    return cache.get(key).get(name);
  }

  private Maybe<String> findVariableInCache(String name) {
    return findVariableInCache(page, name);
  }

  private void putVariable(SourcePage page, String name, Maybe<String> value) {
    String key = page.getFullName();
    if (!cache.containsKey(key)) cache.put(key, new HashMap<String, Maybe<String>>());
    cache.get(key).put(name, value);
  }

  public void putVariable(String name, String value) {
    putVariable(page, name, new Maybe<String>(value));
  }

  public Maybe<String> findVariable(String name) {
    Maybe<String> result = findSpecialVariableValue(name);
    if (!result.isNothing()) return result;

    result = findVariableInPages(name);
    if (!result.isNothing()) return result;

    return findVariableInContext(name);
  }

  private Maybe<String> findSpecialVariableValue(String key) {
    String value;
    if (key.equals("RUNNING_PAGE_NAME"))
      value = page.getName();
    else if (key.equals("RUNNING_PAGE_PATH"))
      value = page.getPath();
    else if (key.equals("PAGE_NAME"))
      value = namedPage.getName();
    else if (key.equals("PAGE_PATH"))
      value = namedPage.getPath();
    else if (key.equals("FITNESSE_PORT")) {
      Maybe<String> port = findVariableInContext("FITNESSE_PORT");
      value = port.isNothing() ? "-1" : port.getValue();
    } else if (key.equals("FITNESSE_ROOTPATH")) {
      Maybe<String> path = findVariableInContext("FITNESSE_ROOTPATH");
      value = path.isNothing() ? "" : path.getValue();
    } else if (key.equals("FITNESSE_VERSION")) {
      Maybe<String> version = findVariableInContext("FITNESSE_VERSION");
      value = version.isNothing() ? "" : version.getValue();
    } else
      return Maybe.noString;
    return new Maybe<String>(value);
  }

  private Maybe<String> findVariableInPages(String name) {
    Maybe<String> localVariable = findVariableInCache(name);
    if (!localVariable.isNothing()) return new Maybe<String>(localVariable.getValue());
    return lookInParentPages(name);
  }

  private Maybe<String> findVariableInContext(String name) {
    return variableSource != null ? variableSource.findVariable(name) : Maybe.noString;
  }

  private Maybe<String> lookInParentPages(String name) {
    for (SourcePage sourcePage : page.getAncestors()) {
      if (!inCache(sourcePage)) {
        Parser.make(copyForPage(sourcePage), sourcePage.getContent()).parse();
        putVariable(sourcePage, "", Maybe.noString);
      }
      Maybe<String> result = findVariableInCache(sourcePage, name);
      if (!result.isNothing()) return result;
    }
    return Maybe.noString;
  }
}
