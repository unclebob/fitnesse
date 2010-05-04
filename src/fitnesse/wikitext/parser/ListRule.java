package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

public class ListRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Symbol list = parser.getCurrent();
        return populateList(parser, list);
    }

    private Maybe<Symbol> populateList(Parser parser, Symbol list) {
        Symbol nextSymbol = list;
        while (true) {
            if (indent(nextSymbol) < indent(list)) break;
            if (nextSymbol != list) parser.moveNext(1);
            if (indent(nextSymbol) > indent(list)) {
                Maybe<Symbol> subList = populateList(parser, nextSymbol);
                if (subList.isNothing()) return Symbol.Nothing;
                list.add(subList.getValue());
            }
            else {
                Symbol body = parser.parseTo(SymbolType.Newline);
                if (parser.getScanner().isEnd()) return Symbol.Nothing;
                list.add(body);
            }
            List<Symbol> nextSymbols = parser.peek(new SymbolType[] {list.getType()});
            if (nextSymbols.size() == 0) break;
            nextSymbol = nextSymbols.get(0);
        }
        return new Maybe<Symbol>(list);
    }

    private int indent(Symbol symbol) {
        String content = symbol.getContent();
        int result = 0;
        while (Character.isWhitespace(content.charAt(result))) result++;
        return result;
    }
}
