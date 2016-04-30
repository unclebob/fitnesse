package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class Paths {
    private Translator translator;

    public Paths(Translator translator) {
        this.translator = translator;
    }

    public List<String> getPaths(Symbol syntaxTree) {
        TreeWalker walker = new TreeWalker();
        syntaxTree.walkPostOrder(walker);
        return walker.result;
    }

    private class TreeWalker implements SymbolTreeWalker {
        public List<String> result = new ArrayList<>();

        @Override
        public boolean visit(Symbol node) {
            if (node.getType() instanceof PathsProvider) {
                result.addAll(((PathsProvider) node.getType()).providePaths(translator, node));
            }
            return true;
        }

        @Override
        public boolean visitChildren(Symbol node) { return true; }
    }
}
