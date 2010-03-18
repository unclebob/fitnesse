package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class HorizontalRuleToken extends ContentToken {

    public HorizontalRuleToken(String content) { super(content); }
    public HorizontalRuleToken() { this(""); }

    public TokenMatch makeMatch(ScanString input) {
        if (input.startsWith("----")) {
            int size = 1;
            while (input.charAt(size + 3) == '-') size++;
            return new TokenMatch(new HorizontalRuleToken(Integer.toString(size)), size + 3);
        }
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        HtmlTag html = new HtmlTag("hr");
        if (!getContent().equals("1")) html.addAttribute("size", getContent());
        return new Maybe<String>(html.html());
    }
    
    public boolean sameAs(Token other) {
        return other instanceof HorizontalRuleToken;
    }
}
