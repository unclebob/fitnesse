package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

public abstract class Rule {
    private WikiPage page;

    public abstract Maybe<Symbol> parse(Scanner scanner);

    public WikiPage getPage() { return page; }
    public void setPage(WikiPage page) { this.page = page; }
}
