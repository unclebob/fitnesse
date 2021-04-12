package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.SourcePage;

public class WikiWordBuilder {
    private final SourcePage currentPage;
    private final String linkBody;
    private final String wikiWordPath;
    private final String qualifiedName;

    public WikiWordBuilder(SourcePage currentPage, String pagePath, String linkBody) {
        this.currentPage = currentPage;
        this.linkBody = linkBody;
        this.wikiWordPath = makePath(currentPage, pagePath);
        this.qualifiedName = currentPage.makeFullPathOfTarget(wikiWordPath);
    }

    public String buildLink(String pageSuffix, String originalName) {
      if (currentPage.targetExists(wikiWordPath)) {
        return makeLinkToExistingWikiPage(qualifiedName + pageSuffix, linkBody, null);
      } else if ("FitNesse".equals(originalName)) {
        return "<span class=\"fitnesse\">" + originalName + "</span>";
      } else {
        return makeLinkToNonExistentWikiPage(originalName, currentPage.makeFullPathOfTarget(wikiWordPath));
      }
    }

    public String makeEditabeLink(String originalName) {
      if (currentPage.targetExists(wikiWordPath)) {
        return makeLinkToExistingWikiPage(qualifiedName, linkBody, null) + " " +
            makeLinkToExistingWikiPage(qualifiedName + "?edit&amp;redirectToReferer=true&amp;redirectAction=", "(edit)", "edit");
      } else {
        return makeLinkToNonExistentWikiPage(originalName, currentPage.makeFullPathOfTarget(wikiWordPath));
      }
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

    private String makeLinkToExistingWikiPage(String qualifiedName, String linkBody, String linkClass) {
        HtmlTag link = new HtmlTag("a", linkBody);
        link.addAttribute("href", qualifiedName);
        if (linkClass != null) {
          link.addAttribute("class", linkClass);
        }
        return link.htmlInline();
    }

    private String makeLinkToNonExistentWikiPage(String text, String url) {
        HtmlTag link = new HtmlTag("a", "[?]");
        link.addAttribute("title", "create page");
        link.addAttribute("href", url+ "?edit&amp;nonExistent=true");
        return text + link.htmlInline();
    }

    private String makeParentPath(SourcePage page, String content) {
        return page.findParentPath(content.substring(1));
    }

    private String makeChildPath(SourcePage page, String content) {
        return String.format("%s.%s", page.getName(), content.substring(1));
    }

}
