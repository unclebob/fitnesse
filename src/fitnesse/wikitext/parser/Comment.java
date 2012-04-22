package fitnesse.wikitext.parser;

import util.Maybe;

public class Comment extends SymbolType implements Rule, Translation {
    public static final Comment symbolType = new Comment();

    public Comment() {
        super("Comment");
        wikiMatcher(new Matcher().startLine(false).string("#"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String literal = parser.parseLiteral(SymbolType.Newline);
        if (!parser.atEnd()) literal += "\n";
        return new Maybe<Symbol>(current.add(literal));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        return "";
    }
}
