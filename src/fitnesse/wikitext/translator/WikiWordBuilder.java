package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.wikitext.parser.SourcePage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.widgets.WikiWordWidget;
import util.GracefulNamer;

public class WikiWordBuilder implements Translation  {
    public String toHtml(Translator translator, Symbol symbol) {
        return buildLink(
                translator.getPage(),
                symbol.getContent(),
                new HtmlText(formatWikiWord(symbol.getContent(), symbol)).html());
    }

    private String buildLink(SourcePage currentPage, String pagePath, String linkBody) {
         return buildLink(currentPage, pagePath, "", linkBody, pagePath);
    }

    public String buildLink(SourcePage currentPage, String pagePath, String pageSuffix, String linkBody, String originalName) {
        String wikiWordPath = makePath(currentPage, pagePath);
        String qualifiedName = currentPage.makeFullPathOfTarget(wikiWordPath);
        if (currentPage.targetExists(wikiWordPath)) {
            return makeLinkToExistingWikiPage(qualifiedName + pageSuffix, linkBody);
        }
        else
            return makeLinkToNonExistentWikiPage(originalName, currentPage.makeUrl(wikiWordPath));
    }

    public String makeLinkToExistingWikiPage(String qualifiedName, String linkBody) {
        HtmlTag link = new HtmlTag("a", linkBody);
        link.addAttribute("href", qualifiedName);
        return link.htmlInline();
    }

    private String formatWikiWord(String originalName, Symbol symbol) {
        String regraceOption = symbol.getVariable(WikiWordWidget.REGRACE_LINK, "");
        return regraceOption.equals("true") ? GracefulNamer.regrace(originalName) : originalName;
    }

    private String makeLinkToNonExistentWikiPage(String text, String url) {
        HtmlTag link = new HtmlTag("a", "[?]");
        link.addAttribute("title", "create page");
        link.addAttribute("href", url+ "?edit&nonExistent=true");
        return new HtmlText(text).html() + link.htmlInline();
    }

    public String makePath(SourcePage page, String content) {
        if (content.startsWith("^") || content.startsWith(">")) {
            return makeChildPath(page, content);
        }
        if (content.startsWith("<")) {
            return makeParentPath(page, content);
        }
        return content;
    }

    private String makeParentPath(SourcePage page, String content) {
        return page.findParentPath(content.substring(1));
    }

    private String makeChildPath(SourcePage page, String content) {
        return String.format("%s.%s", page.getName(), content.substring(1));
    }
}
