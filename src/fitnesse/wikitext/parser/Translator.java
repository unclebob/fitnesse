package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import util.Maybe;

import java.util.ArrayList;

public class Translator {
    private WikiPage currentPage;

    public Translator(WikiPage currentPage) { this.currentPage = currentPage; }

    public String translate(String input) {
        return translate(new Scanner(input), TokenType.Empty);
    }

    public String translate(Scanner scanner, TokenType terminator) {
        return translate(scanner, new TokenType[] {terminator});
    }

    public String translate(Scanner scanner, TokenType[] terminators) {
        StringBuilder result = new StringBuilder();
        ArrayList<TokenType> ignore = new ArrayList<TokenType>();
        for (TokenType terminator: terminators) ignore.add(terminator);
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
