package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;

import java.util.*;

/**
 * Generates a ordered list of all headers from within the current wiki page.
 */
public class Headings extends SymbolType implements Rule, Translation {

  public static final Headings symbolType = new Headings();

  private static final String STYLE = "STYLE";
  private static final String[] OPTION_KEYS = new String[]{STYLE};

  public Headings() {
    super("Headings");
    wikiMatcher(new Matcher().startLineOrCell().string("!headings"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    final Symbol body = parser.parseToEnd(SymbolType.Newline);
    new OptionParser(current, body).parse();
    current.add(body);
    return new Maybe<>(current);
  }

  @Override
  public String toTarget(Translator translator, Symbol current) {
    final List<Symbol> headerLines = findHeaderLines(((HtmlTranslator)translator).getSyntaxTree());
    HeadingContentBuilder headingContentBuilder = new HeadingContentBuilder(headerLines,
      ListStyle.byNameIgnoreCase(current.getProperty(STYLE)));
    HtmlElement html = headingContentBuilder.htmlElements();
    return html.html();
  }

  private List<Symbol> findHeaderLines(Symbol tree) {
    final List<Symbol> symbols = new LinkedList<>();
    for (final Symbol symbol : tree.getChildren()) {
      if (symbol.isType(HeaderLine.symbolType)) {
        symbols.add(symbol);
      }
    }
    return Collections.unmodifiableList(symbols);
  }

  /**
   * Allowed values of an ordered list from CSS.
   */
  public enum ListStyle {

    DECIMAL("decimal"), DECIMAL_LEADING_ZERO("decimal-leading-zero"), LOWER_ROMAN("lower-roman"),
    UPPER_ROMAN("upper-roman"), LOWER_ALPHA("lower-alpha"), UPPER_ALPHA("upper-alpha"),
    NONE("none");

    private final String name;

    ListStyle(final String name) {
      this.name = name;
    }

    static ListStyle byNameIgnoreCase(String name) {
      for (final ListStyle listStyle : values()) {
        if (listStyle.name.equalsIgnoreCase(name)) {
          return listStyle;
        }
      }
      return DECIMAL;
    }

  }

  static String extractTextFromHeaderLine(final Symbol headerLine) {
    final StringBuilder sb = new StringBuilder();
    headerLine.walkPreOrder(new SymbolTreeWalker() {
      @Override
      public boolean visit(final Symbol node) {
        if (node.isType(SymbolType.Text) || node.isType(Literal.symbolType) ||
          node.isType(Whitespace)) {
          sb.append(node.getContent());
        }
        return true;
      }

      @Override
      public boolean visitChildren(final Symbol node) {
        return true;
      }
    });
    return sb.toString();
  }

  static String buildIdOfHeaderLine(final String textFromHeaderLine) {
	  return HtmlUtil.remainRfc3986UnreservedCharacters(textFromHeaderLine);
  }

  class HeadingContentBuilder {

    private final List<Symbol> headerLines;
    private final Stack<HtmlTag> stack = new Stack<>();
    private ListStyle listStyle = ListStyle.DECIMAL;
    private HtmlTag rootElement = null;
    private boolean processed;

    HeadingContentBuilder(final List<Symbol> headerLines, final ListStyle listStyle) {
      this.headerLines = headerLines;
      this.listStyle = listStyle;
      rootElement = new HtmlTag("div");
      rootElement.addAttribute("class", "contents");
      rootElement.add(HtmlUtil.makeBold("Contents:"));
      stack.push(rootElement);
    }

    HtmlElement htmlElements() {
      for (final Symbol headerLine : headerLines) {
        processed = false;
        htmlElements(headerLine);
      }
      return rootElement;
    }

    private void htmlElements(final Symbol headerLine) {
      addListElement(headerLine);
      goToParent(headerLine);
      addListItemElement(headerLine);
      if (!processed) {
        htmlElements(headerLine);
      }
    }

    private void addListElement(final Symbol headerLine) {
      if (getLevel(headerLine) > currentLevel()) {
        HtmlTag listElement = new HtmlTag("ol");
        listElement.addAttribute("style", "list-style-type: " + listStyle.name + ";");
        stack.peek().add(listElement);
        stack.push(listElement);
      }
    }

    private void goToParent(final Symbol headerLine) {
      if (getLevel(headerLine) < currentLevel()) {
        stack.pop();
      }
    }

    private void addListItemElement(final Symbol headerLine) {
      if (getLevel(headerLine) == currentLevel()) {
        final HtmlTag listitemElement = new HtmlTag("li");
        listitemElement.addAttribute("class", "heading" + currentLevel());
        final String textFromHeaderLine = extractTextFromHeaderLine(headerLine);
        final HtmlTag anchorElement = new HtmlTag("a", textFromHeaderLine);
        anchorElement.addAttribute("href",
          "#" + buildIdOfHeaderLine(textFromHeaderLine));
        listitemElement.add(anchorElement);
        stack.peek().add(listitemElement);
        processed = true;
      }
    }

    private int currentLevel() {
      return stack.size() - 1;
    }

    private int getLevel(final Symbol headerLine) {
      return Integer.parseInt(headerLine.getProperty(LineRule.Level));
    }

  }

  class OptionParser {

    private final Symbol current;
    private final Symbol body;

    private String previousOption = null;

    OptionParser(final Symbol current, final Symbol body) {
      this.current = current;
      this.body = body;
    }

    void parse() {
      for (final Symbol option : body.getChildren()) {
        handleSymbol(option);
      }
      finishSymbols();
    }

    private void handleSymbol(final Symbol option) {
      if (!option.isType(SymbolType.Whitespace)) {
        handleNonWhitespace(option);
      }
    }

    private void finishSymbols() {
      handleOptionAsValue(null);
    }

    private void handleNonWhitespace(final Symbol symbol) {
      String option = symbol.getContent();
      if (isOptionAKey(option)) {
        handleOptionAsKeyCandidate(option);
      } else {
        handleOptionAsValue(option);
      }
    }

    private void handleOptionAsValue(final String option) {
      if (isOptionAKey(previousOption)) {
        addToOptions(option);
      }
      previousOption = null;
    }

    private void handleOptionAsKeyCandidate(final String option) {
      if (isOptionAKey(previousOption)) {
        addToOptions(null);
      }
      previousOption = option;
    }

    private boolean isOptionAKey(final String candidate) {
      return candidate != null
        && candidate.startsWith("-")
        && Arrays.asList(OPTION_KEYS).contains(normalizeOptionKey(candidate));
    }

    private String normalizeOptionKey(final String candidate) {
      return candidate.substring(1).toUpperCase();
    }

    private void addToOptions(final String optionValue) {
      current.putProperty(normalizeOptionKey(previousOption), optionValue);
    }

  }

}
