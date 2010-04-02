package fitnesse.wikitext.translator;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.Symbol;

public class IncludeBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String option = symbol.childAt(0).getContent();
        String pageName = symbol.childAt(1).getContent();
        PageCrawler crawler = translator.getPage().getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(pageName);
        WikiPage includedPage;
        try {
            includedPage = crawler.getSiblingPage(translator.getPage(), pagePath);
            String collapseType = option.equals("-setup") ? "hidden" : "collapsable";
            return CollapsibleBuilder.generateHtml(pageName, includedPage.getData().getHtml(), collapseType);
        } catch (Exception e) {
            return e.toString();
        }
    }
}
