package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class Table extends SymbolType implements Rule, Translation {

    public Table() {
        super("Table");
        wikiMatcher(new Matcher().startLine().string(new String[] {"|", "!|", "-|", "-!|"}));
        wikiRule(this);
        htmlTranslation(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String content = current.getContent();
        if (content.charAt(0) == '-') current.putProperty("hideFirst", "");
        while (true) {
            Symbol row = new Symbol(SymbolType.SymbolList);
            current.add(row);
            while (true) {
                Symbol cell = parseCell(parser, content);
                if (parser.atEnd()) return Symbol.nothing;
                if (containsNewLine(cell)) return Symbol.nothing;
                row.add(cell);
                if (parser.getCurrent().getContent().indexOf("\n") > 0 || parser.atLast()) break;
            }
            if (parser.getCurrent().getContent().indexOf("\n|") < 0 || parser.atLast()) break;
        }
        return new Maybe<Symbol>(current);
    }

    private Symbol parseCell(Parser parser, String content) {
        if (content.indexOf("!") >= 0) {
            return parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.literalTableProvider);
        }
        else
            return parser.parseTo(SymbolType.EndCell);
    }

    private boolean containsNewLine(Symbol cell) {
        for (Symbol child: cell.getChildren()) {
            if (child.isType(SymbolType.Newline)) return true;
        }
        return false;
    }

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag table = new HtmlTag("table");
        if (symbol.hasProperty("class")) {
            table.addAttribute("class", symbol.getProperty("class"));
        }
        else {
            table.addAttribute("border", "1");
            table.addAttribute("cellspacing", "0");
        }
        int longestRow = longestRow(symbol);
        int rowCount = 0;
        for (Symbol child: symbol.getChildren()) {
            rowCount++;
            HtmlTag row = new HtmlTag("tr");
            if (rowCount == 1 && symbol.hasProperty("hideFirst")) {
                row.addAttribute("class", "hidden");
            }
            table.add(row);
            int extraColumnSpan = longestRow - rowLength(child);
            int column = 1;
            for (Symbol grandChild: child.getChildren()) {
                String body = translator.translate(grandChild);
                HtmlTag cell = new HtmlTag("td", body.trim());
                if (extraColumnSpan > 0 && column == rowLength(child))
                    cell.addAttribute("colspan", Integer.toString(extraColumnSpan + 1));
                row.add(cell);
                column++;
            }
        }
        return table.html();
    }

    private int longestRow(Symbol table) {
        int longest = 0;
        for (Symbol row: table.getChildren()) {
            int length = rowLength(row);
            if (length > longest) longest = length;
        }
        return longest;
    }

    private int rowLength(Symbol row) {
        return row.getChildren().size();
    }
}
