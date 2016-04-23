package fitnesse.wikitext.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Symbol {
    private static final List<Symbol> NO_CHILDREN = Collections.emptyList();

    public static final Maybe<Symbol> nothing = new Maybe<>();
    public static final Symbol emptySymbol = new Symbol(SymbolType.Empty);

    private SymbolType type;
    private String content;
    private List<Symbol> children;
    private Map<String,String> variables;
    private Map<String,String> properties;
    private int startOffset = -1;
    private int endOffset = -1;

    public Symbol(SymbolType type) { this(type, ""); }

    public Symbol(SymbolType type, String content) {
        this.type = type;
        this.content = content;
        this.children = NO_CHILDREN;
    }

    public Symbol(SymbolType type, String content, int startOffset) {
        this(type, content);
        this.startOffset = startOffset;
        this.endOffset = startOffset + content.length();
    }

    public Symbol(SymbolType type, String content, int startOffset, int endOffset) {
        this(type, content);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
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
            children = new LinkedList<>();
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
        if (variables == null) variables = new HashMap<>(names.length);
        for (String name: names) {
            Maybe<String> value = source.findVariable(name);
            if (!value.isNothing()) variables.put(name, value.getValue());
        }
    }

    public String getVariable(String name, String defaultValue) {
        return variables != null && variables.containsKey(name) ? variables.get(name) : defaultValue;
    }

    public Symbol putProperty(String key, String value) {
        if (properties == null) properties = new HashMap<>(1);
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

    public boolean hasOffset() {
      return startOffset != -1 && endOffset != -1;
    }

    Symbol setStartOffset(int startOffset) {
      this.startOffset = startOffset;
      return this;
    }

    public int getStartOffset() {
      return startOffset;
    }

    Symbol setEndOffset(int endOffset) {
      this.endOffset = endOffset;
      return this;
    }

    public int getEndOffset() {
      return endOffset;
    }

  public void setType(SymbolType type) {
    this.type = type;
  }
}
