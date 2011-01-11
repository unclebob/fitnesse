package fitnesse.wikitext.parser;

import fitnesse.FitNesseContext;
import util.Maybe;

import java.util.HashMap;

public class ParsingPage {
    private SourcePage page;
    private SourcePage namedPage;
    private HashMap<String, HashMap<String, Maybe<String>>> cache;

    public ParsingPage(SourcePage page) {
        this(page, page, new HashMap<String, HashMap<String, Maybe<String>>>());
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

    public Maybe<String> getSpecialVariableValue(String key) {
        String value;
        if (key.equals("RUNNING_PAGE_NAME"))
            value = page.getName();
        else if (key.equals("RUNNING_PAGE_PATH"))
            value = page.getPath();
        else if (key.equals("PAGE_NAME"))
            value = namedPage.getName();
        else if (key.equals("PAGE_PATH"))
            value = namedPage.getPath();
        else if (key.equals("FITNESSE_PORT"))
            value = Integer.toString(FitNesseContext.globalContext.port);
        else if (key.equals("FITNESSE_ROOTPATH"))
            value = FitNesseContext.globalContext.rootPath;
        else
            return Maybe.noString;
        return new Maybe<String>(value);
    }

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

    public String findVariable(SourcePage page, String name, String defaultValue) {
        Maybe<String> result = findVariable(page, name);
        return result.isNothing() ? defaultValue : result.getValue();
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
