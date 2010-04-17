package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.HashMap;

public class ParsingPage {
    private WikiPage page;
    private HashMap<String, String> variables;

    public ParsingPage(WikiPage page) {
        this.page = page;
        variables = new HashMap<String, String>();
    }

    public WikiPage getPage() { return page; }

    public void putVariable(String name, String value) {
        variables.put(name, value);
    }

    public Maybe<String> lookUpVariable(String name) {
        return variables.containsKey(name) ? new Maybe<String>(variables.get(name)) : Maybe.noString;
    }
}
