package fitnesse.wikitext.parser;

import util.Maybe;
import static java.lang.System.arraycopy;

public class LineRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Parser parser) {
        Scanner scanner = parser.getScanner();
        Symbol result = new Symbol(scanner.getCurrentType(), scanner.getCurrentContent());
        
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;

        Symbol body = Parser.makeEnds(getPage(), scanner, makeEnds(parser)).parse();
        return new Maybe<Symbol>(result.add(body));
    }

    private SymbolType[] makeEnds(Parser parser) {
        SymbolType[] parentEnds = parser.getEnds();
        SymbolType[] parentTerminators = parser.getTerminators();
        SymbolType[] ends = new SymbolType[parentTerminators.length + parentEnds.length + 1];
        arraycopy(parentEnds, 0, ends, 0, parentEnds.length);
        arraycopy(parentTerminators, 0, ends, parentEnds.length, parentTerminators.length);
        ends[parentEnds.length + parentTerminators.length] = SymbolType.Newline;
        return ends;
    }
}
