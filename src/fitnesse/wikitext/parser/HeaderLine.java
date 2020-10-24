package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.ToHtml;

public class HeaderLine extends SymbolType {
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
    htmlTranslation(Translate.with(ToHtml::header).child(0));
  }
}
