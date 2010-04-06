package fitnesse.wikitext.parser;

import util.Maybe;

public class LineRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol result = new Symbol(scanner.getCurrentType(), scanner.getCurrentContent());
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
        Symbol body = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.Newline);
        //if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(result.add(body));
    }
}
