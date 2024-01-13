package fitnesse.wikitext.parser;

import fitnesse.html.HtmlText;
import fitnesse.wikitext.SourcePage;
import util.GracefulNamer;

public class WikiWord extends SymbolType implements Translation {
    public static final WikiWord symbolType = new WikiWord(null);

    public static final String REGRACE_LINK = "REGRACE_LINK";
    public static final String WITH_EDIT = "WITH_EDIT";

    private SourcePage sourcePage;

    public WikiWord(SourcePage sourcePage) {
        super("WikiWord");
        htmlTranslation(this);
        this.sourcePage = sourcePage;
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        if ("true".equals(symbol.getProperty(WITH_EDIT))) {
          return buildEditableLink(
                    symbol.getContent(),
                    formatWikiWord(symbol));
        }
        return buildLink(
                symbol.getContent(),
                formatWikiWord(symbol));
    }

    private String buildLink(String pagePath, String linkBody) {
         return new WikiWordBuilder(sourcePage, pagePath, linkBody).buildLink( "", pagePath);
    }

    private String buildEditableLink(String pagePath, String linkBody) {
        return new WikiWordBuilder(sourcePage, pagePath, linkBody).makeEditabeLink(pagePath);
    }

    private String formatWikiWord(Symbol symbol) {
      return new HtmlText(formatWikiWord(symbol.getContent(), symbol)).html();
    }

    private String formatWikiWord(String originalName, Symbol symbol) {
        String regraceOption = symbol.getVariable(REGRACE_LINK, "");
        return regraceOption.equals("true") ? GracefulNamer.regrace(originalName) : originalName;
    }
}
