package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
    private WikiPage currentPage;

    public Parser(WikiPage currentPage) { this.currentPage = currentPage; }

    public Symbol parse(String input) {
        return parseIgnoreFirst(new Scanner(input), SymbolType.Empty);
    }

    public Symbol parse(Scanner scanner, SymbolType terminator) {
        return parse(scanner, new SymbolType[] {terminator});
    }

    public Symbol parseIgnoreFirst(Scanner scanner, SymbolType terminator) {
        return parseIgnoreFirst(scanner, new SymbolType[] {terminator});
    }

    public Symbol parse(Scanner scanner, SymbolType[] terminators) {
        return parse(scanner, terminators, new SymbolType[] {});
    }

    public Symbol parseIgnoreFirst(Scanner scanner, SymbolType[] terminators) {
        return parse(scanner, terminators, terminators);
    }

    private Symbol parse(Scanner scanner, SymbolType[] terminators, SymbolType[] ignoresFirst) {
        Symbol result = new Symbol(SymbolType.SymbolList);
        ArrayList<SymbolType> ignore = new ArrayList<SymbolType>();
        ignore.addAll(Arrays.asList(ignoresFirst));
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(ignore);
            if (scanner.isEnd()) break;
            Token currentToken = scanner.getCurrent();
            if (contains(terminators, currentToken.getType())) break;
            Rule rule = currentToken.getRule();
            if (rule == null) {
                result.add(currentToken);
                ignore.clear();
            }
            else {
                rule.setPage(currentPage);
                Maybe<Symbol> translation = rule.parse(scanner);
                if (translation.isNothing()) {
                    ignore.add(currentToken.getType());
                    scanner.copy(backup);
                }
                else {
                    result.add(translation.getValue());
                    ignore.clear();
                }
            }
        }
        return result;
    }

    private boolean contains(SymbolType[] terminators, SymbolType currentType) {
        for (SymbolType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }
}
