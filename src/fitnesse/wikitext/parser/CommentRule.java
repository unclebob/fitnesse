package fitnesse.wikitext.parser;

import util.Maybe;

public class CommentRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        scanner.makeLiteral(SymbolType.Newline);
        String literal = scanner.getCurrentContent();
        scanner.moveNext();
        return new Maybe<Symbol>(new Symbol(SymbolType.Comment, literal));
    }
}
