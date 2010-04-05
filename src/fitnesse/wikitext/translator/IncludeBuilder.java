package fitnesse.wikitext.translator;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.CollapsibleRule;
import fitnesse.wikitext.parser.Symbol;

public class IncludeBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String option = symbol.childAt(0).getContent();
        String pageName = symbol.childAt(1).getContent();
        String title = "Included page: " + translator.translate(symbol.childAt(1));
        PageCrawler crawler = translator.getPage().getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(pageName);
        WikiPage includedPage;
        try {
            includedPage = crawler.getSiblingPage(translator.getPage(), pagePath);
            String collapseState = option.equals("-setup") ? CollapsibleRule.ClosedState : CollapsibleRule.OpenState;
            return CollapsibleBuilder.generateHtml(collapseState, title, includedPage.getData().getHtml());
        } catch (Exception e) {
            return e.toString();
        }
    }
}
