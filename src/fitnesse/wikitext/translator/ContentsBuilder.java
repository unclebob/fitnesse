package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Symbol;

public class ContentsBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag div = HtmlUtil.makeDivTag("toc" + 1);
        HtmlTag contentsDiv = HtmlUtil.makeDivTag("contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        HtmlTag list = new HtmlTag("ul");
        ContentsItemBuilder itemBuilder
                = new ContentsItemBuilder(symbol, new VariableFinder(translator, symbol));
        try {
            for (WikiPage child: translator.getPage().getChildren()) {
                HtmlTag listItem = new HtmlTag("li");
                listItem.add(itemBuilder.buildItem(child));
                list.add(listItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        contentsDiv.add(list);
        div.add(contentsDiv);
        return div.html();
    }


}
