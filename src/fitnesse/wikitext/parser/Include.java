package fitnesse.wikitext.parser;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wikitext.ParsingPage;
import fitnesse.wikitext.SourcePage;

import java.util.Collection;
import java.util.Collections;

public class Include extends SymbolType implements Rule, Translation {
  private static final String[] setUpSymbols = {"COLLAPSE_SETUP"};
  public static final String TEARDOWN = "teardown";
  public static final String HELP_ARG = "-h";
  public static final String SETUP_ARG = "-setup";
  public static final String TEARDOWN_ARG = "-teardown";
  public static final String COLLAPSE_ARG = "-c";
  public static final String SEAMLESS_ARG = "-seamless";

  public Include() {
    super("Include");
    wikiMatcher(new Matcher().startLineOrCell().string("!include"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    Symbol next = parser.moveNext(1);
    if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

    next = parser.moveNext(1);
    String option = "";
    if ((next.isType(SymbolType.Text) && next.getContent().startsWith("-")) || next.isType(SymbolType.DateFormatOption)) {
      option = next.getContent() + (next.isType(SymbolType.DateFormatOption) ? parser.moveNext(1).getContent() : "");
      next = parser.moveNext(1);
      if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;
      next = parser.moveNext(1);
    }
    current.add(option);
    if (!next.isType(SymbolType.Text) && !next.isType(WikiWord.symbolType)) return Symbol.nothing;

    String includedPageName = getIncludedPageName(parser, next.getContent());

    SourcePage sourcePage = parser.getPage().getNamedPage();

    // Record the page name anyway, since we might want to show an error if it's invalid
    if (PathParser.isWikiPath(includedPageName)) {
      current.add(new Symbol(new WikiWord(sourcePage), includedPageName));
    } else {
      current.add(includedPageName);
    }

    Maybe<SourcePage> includedPage = sourcePage.findIncludedPage(includedPageName);
    if (includedPage.isNothing()) {
      current.add("").add(new Symbol(SymbolType.Style, "error").add(includedPage.because()));
    } else if (HELP_ARG.equals(option)) {
      String helpText = includedPage.getValue().getProperty(WikiPageProperty.HELP);
      current.add("").add(Parser.make(
        parser.getPage(), helpText).parse());
    } else {
      current.childAt(1).putProperty(WikiWord.WITH_EDIT, "true");
      ParsingPage included = option.equals(SETUP_ARG) || option.equals(TEARDOWN_ARG)
        ? parser.getPage()
        : parser.getPage().copyForNamedPage(includedPage.getValue());
      current.add("").add(Parser.make(
        included,
        includedPage.getValue().getContent())
        .parse());
      if (option.equals(SETUP_ARG)) current.evaluateVariables(setUpSymbols, parser.getVariableSource());
    }

    // Remove trailing newline so we do not introduce excessive whitespace in the page.
    if (parser.peek().isType(SymbolType.Newline)) {
      parser.moveNext(1);
    }

    return new Maybe<>(current);
  }

  private String getIncludedPageName(Parser parser, String nextContent) {
    StringBuilder includedPageName = new StringBuilder(nextContent);
    while (parser.peek().isType(SymbolType.Text) || parser.peek().isType(WikiWord.symbolType)) {
      Symbol remainderOfPageName = parser.moveNext(1);
      includedPageName.append(remainderOfPageName.getContent());
    }
    return includedPageName.toString();
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    if (symbol.getChildren().size() < 4) {
      return translator.translate(symbol.childAt(2));
    }
    String option = symbol.childAt(0).getContent();
    if (option.equals(SEAMLESS_ARG)) {
      return translator.translate(symbol.childAt(3));
    } else if (HELP_ARG.equals(option)) {
      return translator.translate(symbol.childAt(3));
    } else {
      String collapseState = stateForOption(option, symbol);
      String title = "Included page: "
        + translator.translate(symbol.childAt(1));
      Collection<String> extraCollapsibleClass =
        option.equals(TEARDOWN_ARG) ? Collections.singleton(TEARDOWN) : Collections.emptySet();
      return Collapsible.generateHtml(collapseState, title, translator.translate(symbol.childAt(3)), extraCollapsibleClass);
    }
  }

  private String stateForOption(String option, Symbol symbol) {
    return ((option.equals(SETUP_ARG) || option.equals(TEARDOWN_ARG)) && symbol.getVariable("COLLAPSE_SETUP", "true").equals("true"))
      || option.equals(COLLAPSE_ARG)
      ? Collapsible.CLOSED
      : "";
  }
}
