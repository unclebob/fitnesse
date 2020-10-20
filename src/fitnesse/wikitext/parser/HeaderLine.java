package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;

public class HeaderLine extends SymbolType implements Translation {
  public static final HeaderLine symbolType = new HeaderLine();

  public HeaderLine() {
    super("HeaderLine");
    wikiMatcher(new Matcher().startLineOrCell().string("!1"));
    wikiMatcher(new Matcher().startLineOrCell().string("!2"));
    wikiMatcher(new Matcher().startLineOrCell().string("!3"));
    wikiMatcher(new Matcher().startLineOrCell().string("!4"));
    wikiMatcher(new Matcher().startLineOrCell().string("!5"));
    wikiMatcher(new Matcher().startLineOrCell().string("!6"));
    wikiRule(new LineRule());
    htmlTranslation(this);
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    final HtmlTag result = new HtmlTag("h" + symbol.findProperty(LineRule.LEVEL, "1"));
    result.add(translator.translate(symbol.childAt(0)).trim());
    symbol.findProperty(LineRule.ID).ifPresent(id -> result.addAttribute("id", id));
    return result.html();
  }
}
