package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class HorizontalRuleToken extends ContentToken {

    public HorizontalRuleToken(String content) { super(content); }

    public Maybe<String> render(Scanner scanner) {
        HtmlTag html = new HtmlTag("hr");
        if (!getContent().equals("1")) html.addAttribute("size", getContent());
        return new Maybe<String>(html.html());
    }
    
    public TokenType getType() { return TokenType.HorizontalRule; }
}
