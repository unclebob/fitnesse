package fitnesse.wikitext.parser;

import util.Maybe;

public class CommentRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String literal = parser.parseLiteral(SymbolType.Newline);
        return new Maybe<Symbol>(new Symbol(SymbolType.Comment, literal));
    }
}
