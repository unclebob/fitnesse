package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

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

    public static String expandPrefix(WikiPage wikiPage, String theWord) {
      if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
        String prefix = wikiPage.getName();
        return String.format("%s.%s", prefix, theWord.substring(1));
      } else if (theWord.charAt(0) == '<') {
        String undecoratedPath = theWord.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
        for (WikiPage current = wikiPage.getParent(); !current.isRoot(); current = current.getParent()) {
          if (current.getName().equals(target)) {
            pathElements[0] = PathParser.render(current.getPageCrawler().getFullPath());
            return "." + StringUtils.join(Arrays.asList(pathElements), ".");
          }
        }
        return "." + undecoratedPath;
      }
      return theWord;
    }

    public String buildLink(String pageSuffix, String originalName) {
      if (currentPage.targetExists(wikiWordPath)) {
        return makeLinkToExistingWikiPage(qualifiedName + pageSuffix, linkBody, null);
      } else if ("FitNesse".equals(originalName)) {
        return "<span class=\"fitnesse\">" + originalName + "</span>";
      } else {
        return makeLinkToNonExistentWikiPage(originalName, currentPage.makeUrl(wikiWordPath));
      }
    }

    public String makeEditabeLink(String originalName) {
      if (currentPage.targetExists(wikiWordPath)) {
        return makeLinkToExistingWikiPage(qualifiedName, linkBody, null) + " " +
            makeLinkToExistingWikiPage(qualifiedName + "?edit&amp;redirectToReferer=true&amp;redirectAction=", "(edit)", "edit");
      } else {
        return makeLinkToNonExistentWikiPage(originalName, currentPage.makeUrl(wikiWordPath));
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
