package fitnesse.wikitext.parser;

import util.Maybe;

public class ListRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol list = scanner.getCurrent();
        
        Symbol body = parser.parseTo(SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;

        //todo: use scanner look-ahead instead?
        Maybe<Symbol> previous = parser.getPrevious(list.getType());
        if (previous.isNothing()) {
            list.add(body);
            return new Maybe<Symbol>(list);
        }

        Symbol previousList = previous.getValue();
        while (true) {
            if (indent(previousList) == indent(list)) break;
            Maybe<Symbol> lastChild = previousList.getLastChild();
            if (lastChild.isNothing()) break;
            Symbol previousChild = lastChild.getValue();
            if (previousChild.getType() != list.getType()) break;
            previousList = previousChild;
        }

        if (indent(previousList) < indent(list)) {
            list.add(body);
            previousList.add(list);
        }
        else {
            previousList.add(body);
        }
        return previous;
    }

    private int indent(Symbol symbol) {
        String content = symbol.getContent();
        int result = 0;
        while (Character.isWhitespace(content.charAt(result))) result++;
        return result;
    }
}
