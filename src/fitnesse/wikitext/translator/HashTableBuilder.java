package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class HashTableBuilder implements Translation {
    private static final String[] cellClasses = {"hash_key", "hash_value"};

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag table = new HtmlTag("table");
        table.addAttribute("class", "hash_table");
        for (Symbol child: symbol.getChildren()) {
            HtmlTag row = new HtmlTag("tr");
            row.addAttribute("class", "hash_row");
            table.add(row);
            for (int i = 0; i < 2; i++) {
                String body = translator.translate(child.childAt(i));
                HtmlTag cell = new HtmlTag("td", body.trim());
                cell.addAttribute("class", cellClasses[i]);
                row.add(cell);
            }
        }
        return table.html();
   }
}
