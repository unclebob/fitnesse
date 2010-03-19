package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class TableToken implements Token {
    public Maybe<String> render(Scanner scanner) {
        HtmlTag table = new HtmlTag("table");
        table.addAttribute("border", "1");
        table.addAttribute("cellspacing", "0");
        while (true) {
            HtmlTag row = new HtmlTag("tr");
            table.add(row);
            while (true) {
                String body = new Translator().translate(scanner, TokenType.EndCell);
                if (scanner.isEnd()) return Maybe.noString;
                HtmlTag cell = new HtmlTag("td", body);
                row.add(cell);
                if (scanner.getCurrent().toString().indexOf("\n") > 0) break;
            }
            if (scanner.getCurrent().toString().indexOf("\n|") < 0) break;
        }
        return new Maybe<String>(table.html());
    }

    public TokenType getType() { return TokenType.Table; }
}
