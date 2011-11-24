package fitnesse.wikitext.parser;

import fitnesse.html.HtmlText;
import util.GracefulNamer;

public class WikiWord extends SymbolType implements Translation {
    public static WikiWord symbolType = new WikiWord(null);

    public static final String REGRACE_LINK = "REGRACE_LINK";

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
        String regraceOption = symbol.getVariable(REGRACE_LINK, "");
        return regraceOption.equals("true") ? GracefulNamer.regrace(originalName) : originalName;
    }
}
