package fitnesse.wikitext.parser;

import util.Maybe;

public class LinkRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol current = scanner.getCurrent();
        Symbol targetList = parser.parseWithEnds(
                new SymbolProvider().setTypes(SymbolProvider.linkTargetTypes),
                parser.makeEndList(new SymbolType[] {SymbolType.Newline, SymbolType.Whitespace}));
        return new Maybe<Symbol>(current.add(targetList));
    }
}
