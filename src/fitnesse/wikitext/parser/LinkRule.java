package fitnesse.wikitext.parser;

import util.Maybe;

public class LinkRule implements Rule {
    public static final String ImageProperty = "image";
    public static final String Left = "left";
    public static final String Right = "right";

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol targetList = parser.parseWithEnds(
                SymbolProvider.linkTargetProvider,
                new SymbolType[] {SymbolType.Newline, SymbolType.Whitespace});
        return new Maybe<Symbol>(current.add(targetList));
    }
}
