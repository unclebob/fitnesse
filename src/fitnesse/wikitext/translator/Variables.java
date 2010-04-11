package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;
import util.Maybe;

public class Variables {
    private Translator translator;

    public Variables(Translator translator) {
        this.translator = translator;
    }

    public Maybe<Symbol> getSymbol(String name) {
        TreeWalker walker = new TreeWalker(name);
        translator.getSyntaxTree().walk(walker);
        return walker.result;
    }

    public Maybe<Symbol> getSymbolBefore(String name, Symbol before) {
        TreeWalker walker = new TreeWalker(name, before);
        translator.getSyntaxTree().walk(walker);
        return walker.result;
    }

    public Maybe<String> getValue(String name) {
        Maybe<Symbol> symbol = getSymbol(name);
        if (symbol.isNothing()) return Maybe.noString;
        return new Maybe<String>(translator.translate(symbol.getValue()));
    }

    private class TreeWalker implements SymbolTreeWalker {
        private String name;
        private Symbol before;

        public Maybe<Symbol> result = Symbol.Nothing;

        public TreeWalker(String name) { this(name, null); }

        public TreeWalker(String name, Symbol before) {
            this.name = name;
            this.before = before;
        }

        public boolean visit(Symbol node) {
            if (before == node) return false;
            if (node.getType() == SymbolType.Define && node.childAt(0).getContent().equals(name)) {
                result = new Maybe<Symbol>(node.childAt(1));
            }
            return true;
        }
    }
}
