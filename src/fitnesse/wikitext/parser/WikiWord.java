package fitnesse.wikitext.parser;

import fitnesse.html.HtmlText;
import fitnesse.wikitext.widgets.WikiWordWidget;
import util.GracefulNamer;

public class WikiWord extends SymbolType implements Translation {
    public static WikiWord symbolType = new WikiWord(null);

    private SourcePage sourcePage;

    public WikiWord(SourcePage sourcePage) {
        super("WikiWord");
        htmlTranslation(this);
        this.sourcePage = sourcePage;
    }

    public String toTarget(Translator translator, Symbol symbol) {
        return buildLink(
                sourcePage,
                symbol.getContent(),
                new HtmlText(formatWikiWord(symbol.getContent(), symbol)).html());
    }

    private String buildLink(SourcePage currentPage, String pagePath, String linkBody) {
         return new WikiWordBuilder(currentPage, pagePath, linkBody).buildLink( "", pagePath);
    }

    private String formatWikiWord(String originalName, Symbol symbol) {
        String regraceOption = symbol.getVariable(WikiWordWidget.REGRACE_LINK, "");
        return regraceOption.equals("true") ? GracefulNamer.regrace(originalName) : originalName;
    }
}
