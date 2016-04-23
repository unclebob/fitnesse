package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;

public class HashTable extends SymbolType implements Rule, Translation {
    private static final SymbolType[] terminators = new SymbolType[] {SymbolType.Colon, SymbolType.Comma, SymbolType.CloseBrace};

    public HashTable() {
        super("HashTable");
        wikiMatcher(new Matcher().string("!{"));
        wikiRule(this);
        htmlTranslation(this);
    }
    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            current.add(row);
            for (int i = 0; i < 2; i++) {
                Symbol cell = parser.parseToIgnoreFirst(terminators);
                if (parser.atEnd() || cell.getChildren().isEmpty()) return Symbol.nothing;
                row.add(cell);
            }
            if (parser.getCurrent().isType(SymbolType.CloseBrace)) break;
        }
        return new Maybe<>(current);
    }

    private static final String[] cellClasses = {"hash_key", "hash_value"};

    @Override
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
