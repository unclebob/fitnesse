package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public class VariableBuilder implements Translation {

    public String toHtml(Translator translator, Symbol symbol) {
        return findVariable(translator, symbol.childAt(0).getContent());
    }

    private String findVariable(Translator translator, String name) {
        try {
            WikiPage page = translator.getPage();
            while (true) {
                Maybe<String> result = page.getData().getLocalVariable(name);
                if (!result.isNothing()) return result.getValue();
                if (page.getPageCrawler().isRoot(page)) break;
                page = page.getParent();
                if (page == null) break;
            }
            return translator.getPage().getData().getVariable(name);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
