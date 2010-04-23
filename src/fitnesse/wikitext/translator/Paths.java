package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.SymbolType;
import java.util.ArrayList;
import java.util.List;

public class Paths {
    private Translator translator;

    public Paths(Translator translator) {
        this.translator = translator;
    }

    public List<String> getPaths(Symbol syntaxTree) {
        TreeWalker walker = new TreeWalker();
        syntaxTree.walk(walker);
        return walker.result;
    }

    private class TreeWalker implements SymbolTreeWalker {
        public List<String> result = new ArrayList<String>();

        public boolean visit(Symbol node) {
            if (node.getType() == SymbolType.Path) {
                result.add(translator.translate(node.childAt(0)));
            }
            return true;
        }
    }
}
