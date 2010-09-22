package fitnesse.wikitext.parser;

import fitnesse.html.HtmlText;
import fitnesse.wikitext.widgets.WikiWordWidget;
import util.GracefulNamer;

public class WikiWordTranslation implements Translation  {
    public String toTarget(Translator translator, Symbol symbol) {
        return buildLink(
                translator.getPage(),
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
