package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class ListRule implements Rule {
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        return populateList(parser, current);
    }

    private Maybe<Symbol> populateList(Parser parser, Symbol list) {
        Symbol nextSymbol = list;
        while (true) {
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
            List<Symbol> nextSymbols = parser.peek(new SymbolType[] {list.getType()});
            if (nextSymbols.size() == 0) break;
            nextSymbol = nextSymbols.get(0);
        }
        return new Maybe<Symbol>(list);
    }

    private Symbol makeListBody(Parser parser) {
        while (parser.peek(new SymbolType[] {SymbolType.Whitespace}).size() > 0) {
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
