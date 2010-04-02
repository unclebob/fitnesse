package fitnesse.wikitext.parser;

public class Token extends Symbol {
    private Rule rule;

    public Token() { super(); }
    public Token(String content) { super(content); }
    public Token(SymbolType type) { super(type); }
    public Token(SymbolType type, String content) { super(type, content); }

    public Rule getRule() { return rule; }
    public void setRule(Rule rule) { this.rule = rule; }
}
