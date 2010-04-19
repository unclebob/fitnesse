package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import util.Maybe;

public class IncludeRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol include = scanner.getCurrent();

        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
        
        scanner.moveNext();
        String option = "";
        if (scanner.isType(SymbolType.Text) && scanner.getCurrentContent().startsWith("-")) {
            option = scanner.getCurrentContent();
            scanner.moveNext();
            if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
            scanner.moveNext();
        }
        if (!scanner.isType(SymbolType.Text) && !scanner.isType(SymbolType.WikiWord)) return Symbol.Nothing;

        Symbol pageName = scanner.getCurrent();
        include.add(option).add(pageName);

        Maybe<WikiPage> includedPage = findIncludedData(parser.getPage().getPage(), pageName.getContent());
        if (includedPage.isNothing()) {
            include.add(new Symbol(SymbolType.Meta).add(includedPage.because()));
        }
        else {
            ParsingPage included = option.equals("-setup") || option.equals("-teardown")
                    ? parser.getPage()
                    : parser.getPage().copyForNamedPage(includedPage.getValue());
            try {
                include.add("").add(Parser.make(
                                included,
                                includedPage.getValue().getData().getContent())
                                .parse());
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        return new Maybe<Symbol>(include);
    }

    private Maybe<WikiPage> findIncludedData(WikiPage includingPage, String pageName) {
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
                        return new Maybe<WikiPage>(remoteIncludedPage);
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
                return new Maybe<WikiPage>(includedPage);
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
