package fitnesse.wikitext.parser;

import util.Maybe;

public class Table extends SymbolType implements Rule, Translation {

    public Table() {
        super("Table");
        wikiMatcher(new Matcher().startLine().string("|"));
        wikiMatcher(new Matcher().startLine().string("!|"));
        wikiMatcher(new Matcher().startLine().string("-|"));
        wikiMatcher(new Matcher().startLine().string("-!|"));
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
        return (content.indexOf("!") >= 0)
           ? parser.parseToWithSymbols(SymbolType.EndCell, SymbolProvider.literalTableProvider, 1)
           : parser.parseTo(SymbolType.EndCell, 1);
    }

    private boolean containsNewLine(Symbol cell) {
        for (Symbol child: cell.getChildren()) {
            if (child.isType(SymbolType.Newline)) return true;
        }
        return false;
    }

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("table");
        if (symbol.hasProperty("class")) {
            writer.putAttribute("class", symbol.getProperty("class"));
        }
        else {
            writer.putAttribute("border", "1");
            writer.putAttribute("cellspacing", "0");
        }
        int longestRow = longestRow(symbol);
        int rowCount = 0;
        for (Symbol child: symbol.getChildren()) {
            rowCount++;
            writer.startTag("tr");
            if (rowCount == 1 && symbol.hasProperty("hideFirst")) {
                writer.putAttribute("class", "hidden");
            }
            int extraColumnSpan = longestRow - rowLength(child);
            int column = 1;
            for (Symbol grandChild: child.getChildren()) {
                String body = translateCellBody(translator, grandChild);
                writer.startTag("td");
                if (extraColumnSpan > 0 && column == rowLength(child))
                    writer.putAttribute("colspan", Integer.toString(extraColumnSpan + 1));
                writer.putText(body);
                writer.endTag();
                column++;
            }
            writer.endTag();
        }
        writer.endTag();
        return writer.toHtml();
    }

    private String translateCellBody(Translator translator, Symbol cell) {
        StringBuilder result = new StringBuilder(cell.getContent());
        for (int i = 0; i < cell.getChildren().size(); i++) {
            Symbol child = cell.childAt(i);
            String childTranslation = translator.translate(child);
            if (!child.isType(Literal.symbolType)) {
                if (i == 0) childTranslation = trimLeft(childTranslation);
                if (i == cell.getChildren().size() - 1) childTranslation = trimRight(childTranslation);
            }
            result.append(childTranslation);
        }
        return result.toString();
    }

    private String trimLeft(String input) {
        String result = input;
        while (result.length() > 0 && Character.isWhitespace(result.charAt(0))) result = result.substring(1);
        return result;
    }

    private String trimRight(String input) {
        String result = input;
        while (result.length() > 0 && Character.isWhitespace(result.charAt(result.length() - 1))) result = result.substring(0, result.length() - 1);
        return result;
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
