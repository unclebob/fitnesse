package fitnesse.wikitext.parser;

import util.Maybe;

public class LinkRule implements Rule {
    public static final String ImageProperty = "image";
    public static final String Left = "left";
    public static final String Right = "right";

    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol current = scanner.getCurrent();
        Symbol targetList = parser.parseWithEnds(
                new SymbolProvider().setTypes(SymbolProvider.linkTargetTypes),
                parser.makeEndList(new SymbolType[] {SymbolType.Newline, SymbolType.Whitespace}));
        return new Maybe<Symbol>(current.add(targetList));
    }
}
