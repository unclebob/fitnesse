package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.ArrayList;
import java.util.List;

public class Token extends Symbol {
    private static final List<Symbol> emptyList = new ArrayList<Symbol>();

    private String content = "";
    private WikiPage page;
    private Rule rule;

    public Token() {}
    public Token(SymbolType type) { setType(type); }
    public Token(String content) { this.content = content;  }
    public Token(SymbolType type, String content) {
        setType(type);
        this.content = content;
    }

    public Maybe<String> render(Scanner scanner) { return new Maybe<String>(content); }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public WikiPage getPage() { return page; }
    public void setPage(WikiPage page) { this.page = page; }

    public Rule getProduction() { return rule; }
    public void setProduction(Rule rule) { this.rule = rule; }

    public List<Symbol> getChildren() {
        return emptyList;
    }

    public String toHtml() { return content; }
}
