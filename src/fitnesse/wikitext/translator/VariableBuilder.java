package fitnesse.wikitext.translator;

import fitnesse.html.HtmlUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public class VariableBuilder implements Translation {

    public String toHtml(Translator translator, Symbol symbol) {
        String name = symbol.childAt(0).getContent();
        Maybe<String> variable = findVariable(translator, name, symbol);
        return variable.isNothing()
                ? HtmlUtil.metaText("undefined variable: " + name)
                : variable.getValue();
    }

    public Maybe<String> findVariable(Translator translator, String name, Symbol symbol) {
        Maybe<String> result = findVariableInPages(translator, name, symbol);
        if (!result.isNothing()) return result;
        try {
            String oldValue = translator.getPage().getData().getVariable(name);
            return oldValue == null ? Maybe.noString : new Maybe<String>(oldValue);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Maybe<String> findVariableInPages(Translator translator, String name, Symbol symbol) {
        try {
            Maybe<Symbol> result = new Variables(translator)
                    .getSymbolBefore(name, symbol);
            if (!result.isNothing()) return new Maybe<String>(translator.translate(result.getValue()));
            
            for (WikiPage page = translator.getPage().getParent(); page != null; page = page.getParent()) {
                result = new Variables(new Translator(page, page.getData().getSyntaxTree())).getSymbol(name);
                if (!result.isNothing()) return new Maybe<String>(translator.translate(result.getValue()));
                if (page.getPageCrawler().isRoot(page)) break;
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return Maybe.noString;
    }
}
