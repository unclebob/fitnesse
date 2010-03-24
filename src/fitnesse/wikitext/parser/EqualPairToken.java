package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;
import java.util.HashMap;

public class EqualPairToken extends Token {
    private static final HashMap<TokenType, String> tags;
    private static final HashMap<TokenType, String> classes;

    static {
        tags = new HashMap<TokenType, String>();
        tags.put(TokenType.Bold, "b");
        tags.put(TokenType.Italic, "i");
        tags.put(TokenType.Strike, "span");

        classes = new HashMap<TokenType, String>();
        classes.put(TokenType.Strike, "strike");
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, this.getType());
        if (scanner.isEnd()) return Maybe.noString;
        
        HtmlTag html = new HtmlTag(tags.get(getType()));
        if (classes.containsKey(getType())) html.addAttribute("class", classes.get(getType()));
        html.add(body);
        return new Maybe<String>(html.html());
    }
}
