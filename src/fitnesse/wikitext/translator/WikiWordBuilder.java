package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.Symbol;
import util.StringUtil;

import java.util.Arrays;

public class WikiWordBuilder implements Translation  {

    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag link = new HtmlTag("a", new HtmlText(symbol.getContent()));
        link.addAttribute("href", qualifiedName(translator.getPage(), symbol.getContent()));
        return link.html();
    }
    private String qualifiedName(WikiPage page, String content) {
        String wikiWordPath = makePath(page, content);
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        WikiPagePath fullPathOfWikiWord;
        try {
            WikiPage parentPage = page.getParent();
            fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return PathParser.render(fullPathOfWikiWord);
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
