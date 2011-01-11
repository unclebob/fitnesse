package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;

public class WikiWordBuilder {
    private SourcePage currentPage;
    private String pagePath;
    private String linkBody;

    public WikiWordBuilder(SourcePage currentPage, String pagePath, String linkBody) {
        this.currentPage = currentPage;
        this.pagePath = pagePath;
        this.linkBody = linkBody;
    }

    public String buildLink(String pageSuffix, String originalName) {
        String wikiWordPath = makePath(currentPage, pagePath);
        String qualifiedName = currentPage.makeFullPathOfTarget(wikiWordPath);
        if (currentPage.targetExists(wikiWordPath)) {
            return makeLinkToExistingWikiPage(qualifiedName + pageSuffix, linkBody);
        }
        else
            return makeLinkToNonExistentWikiPage(originalName, currentPage.makeUrl(wikiWordPath));
    }

    private String makePath(SourcePage page, String content) {
        if (content.startsWith("^") || content.startsWith(">")) {
            return makeChildPath(page, content);
        }
        if (content.startsWith("<")) {
            return makeParentPath(page, content);
        }
        return content;
    }
    private String makeLinkToExistingWikiPage(String qualifiedName, String linkBody) {
        HtmlTag link = new HtmlTag("a", linkBody);
        link.addAttribute("href", qualifiedName);
        return link.htmlInline();
    }

    private String makeLinkToNonExistentWikiPage(String text, String url) {
        HtmlTag link = new HtmlTag("a", "[?]");
        link.addAttribute("title", "create page");
        link.addAttribute("href", url+ "?edit&nonExistent=true");
        return new HtmlText(text).html() + link.htmlInline();
    }

    private String makeParentPath(SourcePage page, String content) {
        return page.findParentPath(content.substring(1));
    }

    private String makeChildPath(SourcePage page, String content) {
        return String.format("%s.%s", page.getName(), content.substring(1));
    }
}
