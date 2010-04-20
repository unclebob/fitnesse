package fitnesse.wikitext.parser;

import util.Maybe;

public class ListRule implements Rule {
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol list = scanner.getCurrent();
        
        Symbol body = parser.parseTo(SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;

        Maybe<Symbol> previous = parser.getPrevious(list.getType());
        if (!previous.isNothing()) {
            previous.getValue().add(body);
            return previous;
        }
        else {
            list.add(body);
            return new Maybe<Symbol>(list);
        }
    }
}
