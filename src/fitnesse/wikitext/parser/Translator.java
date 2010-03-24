package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;

public class Translator {
    private WikiPage currentPage;

    public Translator(WikiPage currentPage) { this.currentPage = currentPage; }

    public String translate(String input) {
        return translateIgnoreFirst(new Scanner(input), TokenType.Empty);
    }

    public String translate(Scanner scanner, TokenType terminator) {
        return translate(scanner, new TokenType[] {terminator});
    }

    public String translateIgnoreFirst(Scanner scanner, TokenType terminator) {
        return translateIgnoreFirst(scanner, new TokenType[] {terminator});
    }

    public String translate(Scanner scanner, TokenType[] terminators) {
        return translate(scanner, terminators, new TokenType[] {});
    }

    public String translateIgnoreFirst(Scanner scanner, TokenType[] terminators) {
        return translate(scanner, terminators, terminators);
    }

    private String translate(Scanner scanner, TokenType[] terminators, TokenType[] ignoresFirst) {
        StringBuilder result = new StringBuilder();
        ArrayList<TokenType> ignore = new ArrayList<TokenType>();
        ignore.addAll(Arrays.asList(ignoresFirst));
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(ignore);
            if (scanner.isEnd()) break;
            Token currentToken = scanner.getCurrent();
            if (contains(terminators, currentToken.getType())) break;
            currentToken.setPage(currentPage);
            Maybe<String> translation = currentToken.render(scanner);
            if (translation.isNothing()) {
                ignore.add(currentToken.getType());
                scanner.copy(backup);
            }
            else {
                result.append(translation.getValue());
                ignore.clear();
            }
        }
        return result.toString();
    }

    private boolean contains(TokenType[] terminators, TokenType currentType) {
        for (TokenType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }
}
