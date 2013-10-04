package fitnesse.wikitext.parser;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import util.Maybe;

import java.util.HashMap;

public class ParsingPage {

    private SourcePage page;
    private SourcePage namedPage;
    private HashMap<String, HashMap<String, Maybe<String>>> cache;

    public ParsingPage(SourcePage page) {
        this(page, page, new HashMap<String, HashMap<String, Maybe<String>>>());
    }

  public ParsingPage copy() {
      return new ParsingPage(page, page, this.cache);
  }

  public ParsingPage copyForPage(SourcePage page) {
      return new ParsingPage(page, page, this.cache);
  }

    public ParsingPage copyForNamedPage(SourcePage namedPage) {
        return new ParsingPage(this.page, namedPage, this.cache);
    }

    private ParsingPage(SourcePage page, SourcePage namedPage, HashMap<String, HashMap<String, Maybe<String>>> cache) {
        this.page = page;
        this.namedPage = namedPage;
        this.cache = cache;
    }

    public SourcePage getPage() { return page; }
    public SourcePage getNamedPage() { return namedPage; }

    public boolean inCache(SourcePage page) {
        return cache.containsKey(page.getFullName());
    }

    public Maybe<String> findVariable(SourcePage page, String name) {
        String key = page.getFullName();
        if (!cache.containsKey(key)) return Maybe.noString;
        if (!cache.get(key).containsKey(name)) return Maybe.noString;
        return cache.get(key).get(name);
    }

    public Maybe<String> findVariable(String name) {
        return findVariable(page, name);
    }

    public void putVariable(SourcePage page, String name, Maybe<String> value) {
        String key = page.getFullName();
        if (!cache.containsKey(key)) cache.put(key, new HashMap<String, Maybe<String>>());
        cache.get(key).put(name, value);
    }

    public void putVariable(String name, String value) {
        putVariable(page, name, new Maybe<String>(value));
    }
}
