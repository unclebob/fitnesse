package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class TableToken extends Token {
    public Maybe<String> render(Scanner scanner) {
        HtmlTag table = new HtmlTag("table");
        table.addAttribute("border", "1");
        table.addAttribute("cellspacing", "0");
        while (true) {
            HtmlTag row = new HtmlTag("tr");
            table.add(row);
            while (true) {
                String body = makeBody(scanner);
                if (scanner.isEnd()) return Maybe.noString;
                HtmlTag cell = new HtmlTag("td", body.trim());
                row.add(cell);
                if (scanner.getCurrentContent().indexOf("\n") > 0) break;
            }
            if (scanner.getCurrentContent().indexOf("\n|") < 0) break;
        }
        return new Maybe<String>(table.html());
    }

    private String makeBody(Scanner scanner) {
        if (getContent().startsWith("!")) {
            scanner.makeLiteral(TokenType.EndCell);
            String body = scanner.getCurrentContent();
            scanner.moveNext();
            return body;
        }
        return new Translator(getPage()).translate(scanner, TokenType.EndCell);
    }

    public TokenType getType() { return TokenType.Table; }
}
