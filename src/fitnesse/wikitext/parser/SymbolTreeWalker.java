package fitnesse.wikitext.parser;

public interface SymbolTreeWalker {
    boolean visit(Symbol node);
    boolean visitChildren(Symbol node);
}
