package fitnesse.wikitext.translator;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.CollapsibleRule;
import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public class IncludeBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String pageName = symbol.childAt(1).getContent();
        Maybe<String> includedContent = findIncludedData(translator.getPage(), pageName);
        if (includedContent.isNothing()) {
            return translator.formatError(includedContent.because());
        }
        else {
            String option = symbol.childAt(0).getContent();
            if (option.equals("-seamless")) {
                return includedContent.getValue();
            }
            else {
                String collapseState = stateForOption(option);
                String title = "Included page: " + translator.translate(symbol.childAt(1));
                return CollapsibleBuilder.generateHtml(collapseState, title, includedContent.getValue());
            }
        }
    }

    private String stateForOption(String option) {
        return option.equals("-setup") || option.equals("-c") 
                ? CollapsibleRule.ClosedState
                : CollapsibleRule.OpenState;
    }

    private Maybe<String> findIncludedData(WikiPage includingPage, String pageName) {
        PageCrawler crawler = includingPage.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(pageName);
        try {
            WikiPage includedPage = crawler.getSiblingPage(includingPage, pagePath);
            if (includedPage == null) {
                if (includingPage instanceof ProxyPage) {
                    ProxyPage proxy = (ProxyPage) includingPage;
                    String host = proxy.getHost();
                    int port = proxy.getHostPort();
                    try {
                        ProxyPage remoteIncludedPage = new ProxyPage("RemoteIncludedPage", null, host, port, pagePath);
                        return new Maybe<String>(remoteIncludedPage.getData().getHtml());
                    }
                    catch (Exception e) {
                        return Maybe.nothingBecause("Remote page \" + host + \":\" + port + \"/\" + pageName + \" does not exist.\n");
                    }
                } else {
                    return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not exist.\n");
                }
            }
            else if (isParentOf(includedPage, includingPage))
               return Maybe.nothingBecause( "Error! Cannot include parent page (" + pageName + ").\n");
            else {
                return new Maybe<String>(includedPage.getData().getHtml());
            }
        }
        catch (Exception e) {
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
