package fitnesse.wikitext.translator;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public class VariableBuilder implements Translation {

    public String toHtml(Translator translator, Symbol symbol) {
        return findVariable(translator.getPage(), symbol.childAt(0).getContent()).getValue();
    }

    public Maybe<String> findVariable(WikiPage currentPage, String name) {
        Maybe<String> result = findVariableInPages(currentPage, name);
        if (!result.isNothing()) return result;
        try {
            String oldValue = currentPage.getData().getVariable(name);
            return oldValue == null ? Maybe.noString : new Maybe<String>(oldValue);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Maybe<String> findVariableInPages(WikiPage currentPage, String name) {
        try {
            for (WikiPage page = currentPage; page != null; page = page.getParent()) {
                Maybe<Symbol> result = page.getData().getLocalVariableSymbol(name);
                if (!result.isNothing()) return new Maybe<String>(new Translator(currentPage).translate(result.getValue()));
                if (page.getPageCrawler().isRoot(page)) break;
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return Maybe.noString;
    }
}
