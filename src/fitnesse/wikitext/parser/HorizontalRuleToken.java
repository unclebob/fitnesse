package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class HorizontalRuleToken extends Token {

    public Maybe<String> render(Scanner scanner) {
        HtmlTag html = new HtmlTag("hr");
        int size = getContent().length() - 3;
        if (size > 1) html.addAttribute("size", Integer.toString(size));
        return new Maybe<String>(html.html());
    }
}
