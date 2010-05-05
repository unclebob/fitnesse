package fitnesse.wikitext.parser;

import util.Maybe;

public class LineRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol result = scanner.getCurrent();
        
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.nothing;

        return new Maybe<Symbol>(result.add(parser.parseWithEnds(new SymbolType[] {SymbolType.Newline})));
    }

}
