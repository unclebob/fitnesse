package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import util.StringUtil;

import java.util.Arrays;

public class WikiWordBuilder {
    private SourcePage currentPage;
    private String pagePath;
    private String linkBody;

    public WikiWordBuilder(SourcePage currentPage, String pagePath, String linkBody) {
        this.currentPage = currentPage;
        this.pagePath = pagePath;
        this.linkBody = linkBody;
    }

    public static String expandPrefix(WikiPage wikiPage, String theWord) throws Exception {
      PageCrawler crawler = wikiPage.getPageCrawler();
      if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
        String prefix = wikiPage.getName();
        return String.format("%s.%s", prefix, theWord.substring(1));
      } else if (theWord.charAt(0) == '<') {
        String undecoratedPath = theWord.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
        for (WikiPage current = wikiPage.getParent(); !crawler.isRoot(current); current = current.getParent()) {
          if (current.getName().equals(target)) {
            pathElements[0] = PathParser.render(crawler.getFullPath(current));
            return "." + StringUtil.join(Arrays.asList(pathElements), ".");
          }
        }
        return "." + undecoratedPath;
      }
      return theWord;
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
