package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;
import util.Maybe;

public class Variables {
    private Symbol syntaxTree;
    private WikiPage page;

    public Variables(WikiPage page, Symbol syntaxTree) {
        this.syntaxTree = syntaxTree;
        this.page = page;
    }

    public Maybe<Symbol> getSymbol(String name) {
        TreeWalker walker = new TreeWalker(name);
        syntaxTree.walk(walker);
        return walker.result;
    }

    public Maybe<String> getValue(String name) {
        Maybe<Symbol> symbol = getSymbol(name);
        if (symbol.isNothing()) return Maybe.noString;
        return new Maybe<String>(new Translator(page).translate(symbol.getValue()));
    }

    private class TreeWalker implements SymbolTreeWalker {
        private String name;
        public Maybe<Symbol> result = Symbol.Nothing;

        public TreeWalker(String name) { this.name = name; }

        public boolean visit(Symbol node) {
            if (node.getType() == SymbolType.Define && node.childAt(0).getContent().equals(name)) {
                result = new Maybe<Symbol>(node.childAt(1));
                return false;
            }
            return true;
        }
    }
}
