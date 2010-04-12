package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;
import util.Maybe;

public class VariableFinder implements VariableSource {
    private Translator translator;
    private Symbol currentSymbol;

    public VariableFinder(Translator translator, Symbol currentSymbol) {
        this.translator = translator;
        this.currentSymbol = currentSymbol;
    }

    public Maybe<String> findVariable(String name) {
        Maybe<String> result = findVariableInPages(name);
        if (!result.isNothing()) return result;
        try {
            String oldValue = translator.getPage().getData().getVariable(name);
            return oldValue == null ? Maybe.noString : new Maybe<String>(oldValue);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Maybe<String> findVariableInPages(String name) {
        try {
            Maybe<Symbol> result = getSymbolBefore(name, translator.getSyntaxTree());
            if (!result.isNothing()) return new Maybe<String>(translator.translate(result.getValue()));

            for (WikiPage page = translator.getPage().getParent(); page != null; page = page.getParent()) {
                result = getSymbol(name, page.getData().getSyntaxTree());
                if (!result.isNothing()) return new Maybe<String>(translator.translate(result.getValue()));
                if (page.getPageCrawler().isRoot(page)) break;
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return Maybe.noString;
    }
    public Maybe<Symbol> getSymbol(String name, Symbol syntaxTree) {
        TreeWalker walker = new TreeWalker(name);
        syntaxTree.walk(walker);
        return walker.result;
    }

    public Maybe<Symbol> getSymbolBefore(String name, Symbol syntaxTree) {
        TreeWalker walker = new TreeWalker(name, currentSymbol);
        syntaxTree.walk(walker);
        return walker.result;
    }

    public Maybe<String> getValue(String name, Symbol syntaxTree) {
        Maybe<Symbol> symbol = getSymbol(name, syntaxTree);
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
