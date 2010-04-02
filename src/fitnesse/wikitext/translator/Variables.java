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

    public Maybe<String> getValue(String name) {
        return getValue(syntaxTree, name);
    }

    private Maybe<String> getValue(Symbol tree, String name) {
        if (tree.getType() == SymbolType.Define) {
            if (tree.childAt(0).getContent().equals(name)) {
                return new Maybe<String>(new Translator(page).translateToHtml(tree.childAt(1)));
            }
            else {
                return Maybe.noString;
            }
        }
        for (Symbol child: tree.getChildren()) {
            Maybe<String> value = getValue(child, name);
            if (!value.isNothing()) return value;
        }
        return Maybe.noString;
    }
}
