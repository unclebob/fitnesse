package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.List;

public abstract class Symbol {
    public static Maybe<Symbol> Nothing = new Maybe<Symbol>();

    private SymbolType type;

    public SymbolType getType() { return type; }
    public void setType(SymbolType type) { this.type = type; }
    
    public Symbol childAt(int index) { return getChildren().get(index); }

    public abstract List<Symbol> getChildren();
    public abstract String toHtml();
}
