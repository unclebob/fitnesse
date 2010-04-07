package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import util.Maybe;

public class ContentsRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        return new Maybe<Symbol>(new Symbol(SymbolType.Contents));
    }
}
