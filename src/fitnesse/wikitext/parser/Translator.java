package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.ArrayList;

public class Translator {
    public String translate(String input) {
        return translate(new Scanner(input), new EmptyToken());
    }

    public String translate(Scanner scanner, Token terminator) {
        StringBuilder result = new StringBuilder();
        ArrayList<Token> ignore = new ArrayList<Token>();
        ignore.add(terminator);
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(ignore);
            if (scanner.isEnd()) break;
            Token currentToken = scanner.getCurrent();
            if (currentToken.sameAs(terminator)) break;
            Maybe<String> translation = currentToken.render(scanner);
            if (translation.isNothing()) {
                ignore.add(currentToken);
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
