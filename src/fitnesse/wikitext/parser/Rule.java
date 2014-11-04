package fitnesse.wikitext.parser;

public interface Rule {
    Maybe<Symbol> parse(Symbol current, Parser parser);
}
