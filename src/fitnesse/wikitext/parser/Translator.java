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
        StringBuilder result = new StringBuilder();
        ArrayList<TokenType> ignore = new ArrayList<TokenType>();
        ignore.add(terminator);
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(ignore);
            if (scanner.isEnd()) break;
            Token currentToken = scanner.getCurrent();
            if (currentToken.getType() == terminator) break;
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
}
