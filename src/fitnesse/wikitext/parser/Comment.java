package fitnesse.wikitext.parser;

public class Comment extends SymbolType implements Rule, Translation {
    public static final Comment symbolType = new Comment();

    public Comment() {
        super("Comment", Newline);
        wikiMatcher(new Matcher().startLine().string("#"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String literal = parser.parseLiteral(Newline);
        if (!parser.atEnd()) literal += "\n";
        return new Maybe<>(current.add(literal));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        return "";
    }
}
