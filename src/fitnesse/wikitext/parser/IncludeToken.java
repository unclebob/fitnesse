package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import util.Maybe;

public class IncludeToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Whitespace) return Maybe.noString;
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Word) return Maybe.noString;
        String pageName = scanner.getCurrent().getContent();
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Newline) return Maybe.noString;

        PageCrawler crawler = getPage().getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(pageName);
        WikiPage includedPage;
        try {
            includedPage = crawler.getSiblingPage(getPage(), pagePath);
            return new Maybe<String>(new CollapsibleToken("collapsable").generateHtml("", includedPage.getData().getContent()));
        } catch (Exception e) {
            return new Maybe<String>(e.toString());
        }
    }
}
