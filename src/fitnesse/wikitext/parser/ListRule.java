package fitnesse.wikitext.parser;

import util.Maybe;

public class ListRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Symbol list = parser.getCurrent();
        return populateList(parser, list);
    }

    private Maybe<Symbol> populateList(Parser parser, Symbol list) {
        Symbol nextSymbol = list;
        while (true) {
            if (nextSymbol.getType() != list.getType()) break;
            if (indent(nextSymbol) < indent(list)) break;
            if (nextSymbol != list) parser.getScanner().moveNext();
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
            nextSymbol = parser.peek();
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
