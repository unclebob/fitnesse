package fitnesse.wikitext.translator;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;

public class ContentsBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag div = HtmlUtil.makeDivTag("toc" + 1);
        HtmlTag contentsDiv = HtmlUtil.makeDivTag("contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        HtmlTag list = new HtmlTag("ul");
        try {
            for (WikiPage child: translator.getPage().getChildren()) {
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
        return div.html();
    }

    private String getHref(WikiPage wikiPage) throws Exception {
      return PathParser.render(wikiPage.getPageCrawler().getFullPath(wikiPage));
    }

    private HtmlElement getLinkText(WikiPage wikiPage) throws Exception {
        return new RawHtml(wikiPage.getName());
    }
}
