package fitnesse.wikitext.parser;

import util.Maybe;
import static java.lang.System.arraycopy;

public class LineRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol result = scanner.getCurrent();
        
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;

        return new Maybe<Symbol>(result.add(parser.parseTo(getPage(), new SymbolType[] {SymbolType.Newline})));
    }

}
