package fitnesse.wikitext.parser;

import util.Maybe;

public class LineRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        Symbol result = new Symbol(scanner.getCurrentType(), scanner.getCurrentContent());
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
        Symbol body = Parser.makeEnds(getPage(), scanner, SymbolType.Newline).parse();
        return new Maybe<Symbol>(result.add(body));
    }
}
