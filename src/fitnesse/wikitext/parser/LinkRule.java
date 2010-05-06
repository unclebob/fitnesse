package fitnesse.wikitext.parser;

import util.Maybe;

public class LinkRule implements Rule {
    public static final String ImageProperty = "image";
    public static final String Left = "left";
    public static final String Right = "right";

    private static final SymbolProvider linkTargetProvider = new SymbolProvider(
            new SymbolType[] {SymbolType.Literal, SymbolType.Variable});

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol targetList = parser.parseWithEnds(
                linkTargetProvider,
                new SymbolType[] {SymbolType.Newline, SymbolType.Whitespace});
        return new Maybe<Symbol>(current.add(targetList));
    }
}
