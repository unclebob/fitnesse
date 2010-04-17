package fitnesse.wikitext.parser;

import fitnesse.wikitext.translator.VariableSource;
import util.Maybe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Symbol {
    public static Maybe<Symbol> Nothing = new Maybe<Symbol>();

    private SymbolType type;
    private String content = "";
    private List<Symbol> children = new ArrayList<Symbol>();
    private HashMap<String ,String> variables;

    public Symbol(SymbolType type) { setType(type); }
    public Symbol() { this(SymbolType.Empty); }

    public Symbol(String content) {
        this();
        this.content = content;
    }

    public Symbol(SymbolType type, String content) {
        this(content);
        setType(type);
    }

    public SymbolType getType() { return type; }
    public void setType(SymbolType type) { this.type = type; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Symbol childAt(int index) { return getChildren().get(index); }
    public List<Symbol> getChildren() { return children; }

    public Symbol add(Symbol child) {
        children.add(child);
        return this;
    }

    public Symbol add(String text) {
        children.add(new Symbol(SymbolType.Text, text));
        return this;
    }

    public void removeLastChild() {
        children.remove(children.size() - 1);
    }

    public Symbol childrenAfter(int after) {
        Symbol result = new Symbol(SymbolType.SymbolList);
        for (int i = after + 1; i < children.size(); i++) result.add(children.get(i));
        return result;
    }

    public void walk(SymbolTreeWalker walker) {
        walk(this, walker);
    }

    private boolean walk(Symbol symbol, SymbolTreeWalker walker) {
        for (Symbol child: symbol.children) {
            if (!walk(child, walker)) return false;
        }
        if (!walker.visit(symbol)) return false;
        return true;
    }

    public void evaluateVariables(String[] names, VariableSource source) {
        if (variables == null) variables = new HashMap<String, String>();
        for (String name: names) {
            Maybe<String> value = source.findVariable(name);
            if (!value.isNothing()) variables.put(name, value.getValue());
        }
    }

    public String getVariable(String name, String defaultValue) {
        return variables != null && variables.containsKey(name) ? variables.get(name) : defaultValue;
    }
}
