package fitnesse.wikitext.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Symbol {
    private static final List<Symbol> NO_CHILDREN = Collections.emptyList();

    public static final Maybe<Symbol> nothing = new Maybe<Symbol>();
    public static final Symbol emptySymbol = new Symbol(SymbolType.Empty);

    private SymbolType type;
    private String content;
    private List<Symbol> children;
    private Map<String,String> variables;
    private Map<String,String> properties;

    public Symbol(SymbolType type) { this(type, ""); }

    public Symbol(SymbolType type, String content) {
        this.type = type;
        this.content = content;
        this.children = NO_CHILDREN;
    }

    public SymbolType getType() { return type; }
    public boolean isType(SymbolType type) { return this.type.matchesFor(type); }
    public boolean isStartCell() { return isType(Table.symbolType) || isType(SymbolType.EndCell); }
    public boolean isStartLine() { return isType(HorizontalRule.symbolType) || isType(Nesting.symbolType); }

    public boolean isLineType() {
        return isType(HeaderLine.symbolType) || isType(SymbolType.CenterLine) || isType(SymbolType.Meta) ||
                isType(SymbolType.NoteLine);
    }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Symbol childAt(int index) { return getChildren().get(index); }
    public Symbol lastChild() { return childAt(getChildren().size() - 1); }
    public List<Symbol> getChildren() { return children; }

    private List<Symbol> children() {
        if (children == NO_CHILDREN) {
            children = new LinkedList<Symbol>();
        }
        return children;
    }

    public Symbol addToFront(Symbol child) {
        children().add(0, child);
        return this;
    }

    public Symbol add(Symbol child) {
        children().add(child);
        return this;
    }

    public Symbol add(String text) {
        children().add(new Symbol(SymbolType.Text, text));
        return this;
    }

    public Symbol childrenAfter(int after) {
        Symbol result = new Symbol(SymbolType.SymbolList);
        for (int i = after + 1; i < children.size(); i++) result.add(children.get(i));
        return result;
    }

    public boolean walkPostOrder(SymbolTreeWalker walker) {
        if (walker.visitChildren(this)) {
            for (Symbol child: children) {
                if (!child.walkPostOrder(walker)) return false;
            }
        }
        return walker.visit(this);
    }

    public boolean walkPreOrder(SymbolTreeWalker walker) {
        if (!walker.visit(this)) return false;
        if (walker.visitChildren(this)) {
            for (Symbol child: children) {
                if (!child.walkPreOrder(walker)) return false;
            }
        }
        return true;
    }

    public void evaluateVariables(String[] names, VariableSource source) {
        if (variables == null) variables = new HashMap<String,String>(names.length);
        for (String name: names) {
            Maybe<String> value = source.findVariable(name);
            if (!value.isNothing()) variables.put(name, value.getValue());
        }
    }

    public String getVariable(String name, String defaultValue) {
        return variables != null && variables.containsKey(name) ? variables.get(name) : defaultValue;
    }

    public Symbol putProperty(String key, String value) {
        if (properties == null) properties = new HashMap<String,String>(1);
        properties.put(key, value);
        return this;
    }

    public boolean hasProperty(String key) {
        return properties != null && properties.containsKey(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties != null && properties.containsKey(key) ? properties.get(key) : defaultValue;
    }

    public String getProperty(String key) {
        return getProperty(key, "");
    }

    public SymbolType closeType() {
        return type == SymbolType.OpenBrace ? SymbolType.CloseBrace
                : type == SymbolType.OpenBracket ? SymbolType.CloseBracket
                : type == SymbolType.OpenParenthesis ? SymbolType.CloseParenthesis
                : type == Literal.symbolType ? SymbolType.CloseLiteral
                : type == Comment.symbolType ? SymbolType.Newline
                : SymbolType.Empty;
    }


}
