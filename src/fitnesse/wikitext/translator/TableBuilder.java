package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class TableBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag table = new HtmlTag("table");
        table.addAttribute("border", "1");
        table.addAttribute("cellspacing", "0");
        for (Symbol child: symbol.getChildren()) {
            HtmlTag row = new HtmlTag("tr");
            table.add(row);
            for (Symbol grandChild: child.getChildren()) {
                String body = translator.translate(grandChild);
                HtmlTag cell = new HtmlTag("td", body.trim());
                row.add(cell);
            }
        }
        return table.html();
    }
}
