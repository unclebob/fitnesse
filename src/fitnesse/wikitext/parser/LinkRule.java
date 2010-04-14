package fitnesse.wikitext.parser;

import util.Maybe;

public class LinkRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol current = scanner.getCurrent();
        Symbol targetList = Parser.makeEnds(
                getPage(),
                scanner,
                new SymbolProvider().setTypes(SymbolProvider.linkTargetTypes),
                parser.makeEndList(new SymbolType[] {SymbolType.Newline, SymbolType.Whitespace})).parse();
        return new Maybe<Symbol>(current.add(targetList));
    }
}
