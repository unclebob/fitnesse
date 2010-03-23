package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class EqualPairToken extends Token {
    public static final EqualPairToken BoldToken = new EqualPairToken("b", "");
    public static final EqualPairToken ItalicToken = new EqualPairToken("i", "");
    public static final EqualPairToken StrikeToken = new EqualPairToken("span", "strike");
    private String tag;
    private String classAttribute;

    public EqualPairToken(String tag, String classAttribute) {
        this.tag = tag;
        this.classAttribute = classAttribute;
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, this.getType());
        if (scanner.isEnd()) return Maybe.noString;
        HtmlTag html = new HtmlTag(tag);
        if (classAttribute.length() > 0) html.addAttribute("class", classAttribute);
        html.add(body);
        return new Maybe<String>(html.html());
    }
}
