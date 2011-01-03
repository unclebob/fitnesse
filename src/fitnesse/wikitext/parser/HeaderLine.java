package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;

public class HeaderLine extends SymbolType implements Translation {
    public HeaderLine() {
        super("HeaderLine");
        wikiMatcher(new Matcher().string("!1"));
        wikiMatcher(new Matcher().string("!2"));
        wikiMatcher(new Matcher().string("!3"));
        wikiMatcher(new Matcher().string("!4"));
        wikiMatcher(new Matcher().string("!5"));
        wikiMatcher(new Matcher().string("!6"));
        wikiRule(new LineRule());
        htmlTranslation(this);
    }

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag("h" + symbol.getProperty(LineRule.Level));
        result.add(translator.translate(symbol.childAt(0)).trim());
        return result.html();
    }
}
