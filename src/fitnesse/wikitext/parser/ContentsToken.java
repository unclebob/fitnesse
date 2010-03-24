package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.Maybe;

import java.util.List;

public class ContentsToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        HtmlTag div = HtmlUtil.makeDivTag("toc" + 1);
        HtmlTag contentsDiv = HtmlUtil.makeDivTag("contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        HtmlTag list = new HtmlTag("ul");
        try {
            for (WikiPage child: getPage().getChildren()) {
                HtmlTag listItem = new HtmlTag("li");
                HtmlTag link = HtmlUtil.makeLink(getHref(child), getLinkText(child));
                listItem.add(link);
                list.add(listItem);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        contentsDiv.add(list);
        div.add(contentsDiv);
        return new Maybe<String>(div.html());
    }

    private String getHref(WikiPage wikiPage) throws Exception {
      return PathParser.render(wikiPage.getPageCrawler().getFullPath(wikiPage));
    }

    private HtmlElement getLinkText(WikiPage wikiPage) throws Exception {
        return new RawHtml(wikiPage.getName());
    }
}
