package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class Phrase extends Symbol {

    private List<Symbol> children = new ArrayList<Symbol>();

    public Phrase(SymbolType type) { setType(type); }

    public Phrase(List<Symbol> children) {
        setType(SymbolType.SymbolList);
        this.children = children; }

    public List<Symbol> getChildren() { return children; }

    @Override
    public String toHtml() {
        return "";
    }

    public Phrase add(Symbol child) {
        children.add(child);
        return this;
    }

    public Phrase add(List<Symbol> grandChildren) {
        children.add(new Phrase(grandChildren));
        return this;
    }

    public Phrase add(String text) {
        children.add(new Token(SymbolType.Text, text));
        return this;
    }
}
