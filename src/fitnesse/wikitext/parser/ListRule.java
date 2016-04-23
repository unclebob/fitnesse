package fitnesse.wikitext.parser;

public class ListRule implements Rule {
    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        return populateList(parser, current);
    }

    private Maybe<Symbol> populateList(Parser parser, Symbol list) {
        Symbol nextSymbol = list;
        while (isList(nextSymbol)) {
            if (indent(nextSymbol) < indent(list)) break;
            if (nextSymbol != list) parser.moveNext(1);
            if (indent(nextSymbol) > indent(list)) {
                Maybe<Symbol> subList = populateList(parser, nextSymbol);
                if (subList.isNothing()) return Symbol.nothing;
                list.lastChild().add(subList.getValue());
            }
            else {
                Symbol body = makeListBody(parser);
                if (parser.atEnd()) return Symbol.nothing;
                list.add(body);
            }
            nextSymbol = parser.peek();
        }
        return new Maybe<>(list);
    }

    private static boolean isList(Symbol symbol) {
        return symbol.isType(SymbolType.OrderedList) || symbol.isType(SymbolType.UnorderedList);
    }

    private Symbol makeListBody(Parser parser) {
        while (!parser.peek(new SymbolType[]{SymbolType.Whitespace}).isEmpty()) {
            parser.moveNext(1);
        }
        return parser.parseTo(SymbolType.Newline, 1);
    }

    private int indent(Symbol symbol) {
        String content = symbol.getContent();
        int result = 0;
        while (Character.isWhitespace(content.charAt(result))) result++;
        return result;
    }
}
