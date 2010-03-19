package fitnesse.wikitext.parser;

import util.Maybe;

public class EndSectionToken implements Token {
    public TokenMatch makeMatch(ScanString input) {
        if (input.startsLine() && input.startsWith("*")) {
            int offset = 1;
            while (input.charAt(offset) == '*') offset++;
            if (input.charAt(offset) == '!') {
                return new TokenMatch(this, offset + 1);
            }
        }
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean sameAs(Token other) {
        return other instanceof EndSectionToken;
    }
}
