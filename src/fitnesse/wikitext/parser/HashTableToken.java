package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.translator.Translator;
import util.Maybe;

public class HashTableToken extends Token {
    private static final String[] cellClasses = {"hash_key", "hash_value"};

    public Maybe<String> render(Scanner scanner) {
        HtmlTag table = new HtmlTag("table");
        table.addAttribute("class", "hash_table");
        while (true) {
            HtmlTag row = new HtmlTag("tr");
            row.addAttribute("class", "hash_row");
            table.add(row);
            for (int i = 0; i < 2; i++) {
                String body = new Translator(getPage()).translateIgnoreFirst(scanner,
                        new SymbolType[] {SymbolType.Colon, SymbolType.Comma, SymbolType.CloseBrace});
                if (scanner.isEnd()) return Maybe.noString;
                HtmlTag cell = new HtmlTag("td", body.trim());
                cell.addAttribute("class", cellClasses[i]);
                row.add(cell);
            }
            if (scanner.isType(SymbolType.CloseBrace)) break;
        }
        return new Maybe<String>(table.html());
    }
}
