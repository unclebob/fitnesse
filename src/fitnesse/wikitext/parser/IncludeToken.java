package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import util.Maybe;

public class IncludeToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (!scanner.isType(TokenType.Whitespace)) return Maybe.noString;
        scanner.moveNext();
        String option = "";
        if (scanner.isType(TokenType.Text) && scanner.getCurrent().getContent().startsWith("-")) {
            option = scanner.getCurrent().getContent();
            scanner.moveNext();
            if (!scanner.isType(TokenType.Whitespace)) return Maybe.noString;
            scanner.moveNext();
        }
        if (!scanner.isType(TokenType.Text)) return Maybe.noString;
        String pageName = scanner.getCurrent().getContent();
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Newline) return Maybe.noString;

        PageCrawler crawler = getPage().getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(pageName);
        WikiPage includedPage;
        try {
            includedPage = crawler.getSiblingPage(getPage(), pagePath);
            String collapseType = option.equals("-setup") ? "hidden" : "collapsable";
            return new Maybe<String>(new CollapsibleToken(collapseType).generateHtml(pageName, includedPage.getData().getContent()));
        } catch (Exception e) {
            return new Maybe<String>(e.toString());
        }
    }
}
