package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
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
        return getSymbol(syntaxTree, name);
    }

    private Maybe<Symbol> getSymbol(Symbol tree, String name) {
        if (tree.getType() == SymbolType.Define) {
            if (tree.childAt(0).getContent().equals(name)) {
                return new Maybe<Symbol>(tree.childAt(1));
            }
            else {
                return Symbol.Nothing;
            }
        }
        for (Symbol child: tree.getChildren()) {
            Maybe<Symbol> value = getSymbol(child, name);
            if (!value.isNothing()) return value;
        }
        return Symbol.Nothing;
    }

    public Maybe<String> getValue(String name) {
        Maybe<Symbol> symbol = getSymbol(syntaxTree, name);
        if (symbol.isNothing()) return Maybe.noString;
        return new Maybe<String>(new Translator(page).translateToHtml(symbol.getValue()));
    }
}
