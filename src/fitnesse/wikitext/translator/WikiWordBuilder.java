package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.widgets.WikiWordWidget;
import util.GracefulNamer;
import util.Maybe;
import util.StringUtil;

import java.util.Arrays;

public class WikiWordBuilder implements Translation  {
    public String toHtml(Translator translator, Symbol symbol) {
        return buildLink(
                translator.getPage(),
                symbol.getContent(),
                new HtmlText(formatWikiWord(translator, symbol.getContent(), symbol)).html());
    }

    private String buildLink(WikiPage currentPage, String pagePath, String linkBody) {
         return buildLink(currentPage, pagePath, "", linkBody, pagePath);
    }

    public String buildLink(WikiPage currentPage, String pagePath, String pageSuffix, String linkBody, String originalName) {
        String wikiWordPath = makePath(currentPage, pagePath);
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        WikiPagePath fullPathOfWikiWord;
        WikiPage targetPage;
        try {
            WikiPage parentPage = currentPage.getParent();
            fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
            targetPage = parentPage.getPageCrawler().getPage(parentPage, pathOfWikiWord);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        String qualifiedName = PathParser.render(fullPathOfWikiWord);
        if (targetPage != null) {
            return makeLinkToExistingWikiPage(qualifiedName + pageSuffix, linkBody);
        }
        else
            return makeLinkToNonExistentWikiPage(originalName, qualifiedName);
    }

    public String makeLinkToExistingWikiPage(String qualifiedName, String linkBody) {
        HtmlTag link = new HtmlTag("a", linkBody);
        link.addAttribute("href", qualifiedName);
        return link.htmlInline();
    }

    private String formatWikiWord(Translator translator, String originalName, Symbol symbol) {
        String regraceOption = symbol.getVariable(WikiWordWidget.REGRACE_LINK, "");
        return regraceOption.equals("true") ? GracefulNamer.regrace(originalName) : originalName;
    }

    private String makeLinkToNonExistentWikiPage(String text, String qualifiedName) {
        HtmlTag link = new HtmlTag("a", "[?]");
        link.addAttribute("title", "create page");
        link.addAttribute("href", qualifiedName + "?edit&nonExistent=true");
        return new HtmlText(text).html() + link.htmlInline();
    }

    public String makePath(WikiPage page, String content) {
        if (content.startsWith("^") || content.startsWith(">")) {
            return makeChildPath(page, content);
        }
        if (content.startsWith("<")) {
            return makeParentPath(page, content);
        }
        return content;
    }

    private String makeParentPath(WikiPage page, String content) {
        String undecoratedPath = content.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        PageCrawler crawler = page.getPageCrawler();
        try {
            WikiPage ancestor = crawler.findAncestorWithName(page, target);
            if (ancestor != null) {
                pathElements[0] = PathParser.render(crawler.getFullPath(ancestor));
                return "." + StringUtil.join(Arrays.asList(pathElements), ".");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return "." + undecoratedPath;
    }

    private String makeChildPath(WikiPage page, String content) {
        return String.format("%s.%s", page.getName(), content.substring(1));
    }
}
