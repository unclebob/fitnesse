package fitnesse.wikitext.parser;

public class NewlineToken extends Token {
    public NewlineToken() { super("\n"); }

    public SymbolType getType() { return SymbolType.Newline; }
}
