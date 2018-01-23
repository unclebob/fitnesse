package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.SymbolUtil;

import java.util.List;

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
    final HtmlTag result = new HtmlTag("h" + symbol.getProperty(LineRule.Level));
    addInnerHtml(result, translator, symbol);
    addAttributeId(result, translator, symbol.childAt(0));
    return result.html();
  }

  private void addInnerHtml(final HtmlTag result, final Translator translator,
                            final Symbol symbol) {
    final String heading = translator.translate(symbol.childAt(0)).trim();
    result.add(heading);
  }

  private void addAttributeId(final HtmlTag result, final Translator translator,
                              final Symbol symbol) {
    final String textFromHeaderLine = Headings.extractTextFromHeaderLine(symbol);
    final String value = Headings.buildIdOfHeaderLine(textFromHeaderLine);
    result.addAttribute("id", value);
  }

}
