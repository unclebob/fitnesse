package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class EqualPairToken extends ContentToken {
    private String tag;
    private String classAttribute;

    public EqualPairToken(String content, String tag, String classAttribute) {
        super(content);
        this.tag = tag;
        this.classAttribute = classAttribute;
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator().translate(scanner, this);
        if (scanner.isEnd()) return Maybe.noString;
        HtmlTag html = new HtmlTag(tag);
        if (classAttribute.length() > 0) html.addAttribute("class", classAttribute);
        html.add(body);
        return new Maybe<String>(html.html());
    }

    public boolean sameAs(Token other) {
        return other instanceof EqualPairToken && ((EqualPairToken)other).getContent().equals(getContent());
    }
}
