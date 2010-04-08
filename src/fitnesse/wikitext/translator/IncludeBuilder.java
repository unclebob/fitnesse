package fitnesse.wikitext.translator;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.CollapsibleRule;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;

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
            if (isParentOf(includedPage, translator.getPage()))
               return translator.translate(new Symbol(SymbolType.Meta).add(String.format("Error! Cannot include parent page (%s).\n", pageName)));
            else {
                String collapseState = option.equals("-setup") ? CollapsibleRule.ClosedState : CollapsibleRule.OpenState;
                return CollapsibleBuilder.generateHtml(collapseState, title, includedPage.getData().getHtml());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isParentOf(WikiPage possibleParent, WikiPage currentPage) {
        try {
            for (WikiPage page = currentPage; page.getParent() != page; page = page.getParent()) {
                if (possibleParent == page)
                  return true;
            }
            return false;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
