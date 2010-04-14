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
        Scanner scanner = parser.getScanner();
        Symbol result = scanner.getCurrent();

        Symbol body = parser.parseTo(getPage(), new SymbolType[] {SymbolType.Newline});
        for (Symbol option: body.getChildren()) {
            if (option.getType() == SymbolType.Whitespace) continue;
            if (!option.getContent().startsWith("-")) return Symbol.Nothing;
            result.add(option);
        }

        return new Maybe<Symbol>(result);
    }
}
