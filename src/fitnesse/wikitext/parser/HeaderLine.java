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
    String text = getText(symbol);
    final String value = HtmlUtil.remainRfc3986UnreservedCharacters(text);
    result.addAttribute("id", value);
  }

  private String getText(final Symbol symbol) {
    final List<Symbol> textSymbols = SymbolUtil.findSymbolsByType(symbol, Text, true);
    final StringBuilder stringBuilder = new StringBuilder(textSymbols.size());
    for (final Symbol texts : textSymbols) {
      stringBuilder.append(texts.getContent());
    }
    return stringBuilder.toString();
  }

}
