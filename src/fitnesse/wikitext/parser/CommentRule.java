package fitnesse.wikitext.parser;

import util.Maybe;

public class CommentRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String literal = parser.parseLiteral(SymbolType.Newline);
        if (!parser.atEnd()) literal += "\n";
        return new Maybe<Symbol>(current.add(literal));
    }
}
