package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;

import java.util.ArrayList;
import java.util.List;


public class Paths {
    private Symbol syntaxTree;
    private WikiPage page;

    public Paths(WikiPage page, Symbol syntaxTree) {
        this.syntaxTree = syntaxTree;
        this.page = page;
    }

    public List<String> getPaths() {
        TreeWalker walker = new TreeWalker();
        syntaxTree.walk(walker);
        return walker.result;
    }

    private class TreeWalker implements SymbolTreeWalker {
        public List<String> result = new ArrayList<String>();

        public boolean visit(Symbol node) {
            if (node.getType() == SymbolType.Path) {
                result.add(new Translator(page).translateToHtml(node.childAt(0)));
            }
            return true;
        }
    }
}
