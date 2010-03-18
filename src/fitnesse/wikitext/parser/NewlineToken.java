package fitnesse.wikitext.parser;

public class NewlineToken extends ContentToken {
    public NewlineToken() { super("\n"); }

    public boolean sameAs(Token other) {
        return other instanceof NewlineToken;
    }
}
