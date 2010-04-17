package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;
import util.Maybe;

import java.util.HashMap;

public class VariableFinder implements VariableSource {
    private ParsingPage page;
    private HashMap<String, Maybe<String>> parentCache = new HashMap<String, Maybe<String>>();

    public VariableFinder(ParsingPage page) {
        this.page = page;
    }

    public Maybe<String> findVariable(String name) {
        Maybe<String> result = findVariableInPages(name);
        if (!result.isNothing()) return result;
        try {
            String oldValue = page.getPage().getData().getVariable(name);
            return oldValue == null ? Maybe.noString : new Maybe<String>(oldValue);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Maybe<String> findVariableInPages(String name) {
        Maybe<String> localVariable = page.lookUpVariable(name);
        if (!localVariable.isNothing()) return new Maybe<String>(localVariable.getValue());

        if (parentCache.containsKey(name)) return parentCache.get(name);

        Maybe<String> result = lookInParentPages(name);
        parentCache.put(name, result);
        return result;
    }

    private Maybe<String> lookInParentPages(String name) {
        try {
            for (WikiPage wikiPage = getParent(page.getPage()); wikiPage != null; wikiPage = getParent(wikiPage)) {
                Maybe<Symbol> result = getSymbol(name, wikiPage.getData().getSyntaxTree());
                if (!result.isNothing()) {
                    return new Maybe<String>(result.getValue().getContent());
                }
                if (wikiPage.getPageCrawler().isRoot(wikiPage)) break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        return Maybe.noString;
    }

    private WikiPage getParent(WikiPage child) {
        if (child == null) return null;
        try {
            WikiPage parent = child.getParent();
            if (parent == child) return null;
            return parent;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public Maybe<Symbol> getSymbol(String name, Symbol syntaxTree) {
        TreeWalker walker = new TreeWalker(name);
        syntaxTree.walk(walker);
        return walker.result;
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
                result = new Maybe<Symbol>(node.childAt(2));
            }
            return true;
        }
    }
}
