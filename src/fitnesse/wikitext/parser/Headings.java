package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.shared.Names;

import java.util.*;

/**
 * Generates a ordered list of all headers from within the current wiki page.
 */
public class Headings extends SymbolType implements Rule, Translation {

  public static final Headings symbolType = new Headings();
  public static final String DEFAULT_STYLE = "decimal";

  private static final String STYLE = "STYLE";

  public Headings() {
    super("Headings");
    wikiMatcher(new Matcher().startLineOrCell().string("!headings"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    final Symbol body = parser.parseToEnd(SymbolType.Newline);

    if (body.getChildren().size() > 3
      && body.childAt(0).getType() == SymbolType.Whitespace
      && body.childAt(1).getType() == SymbolType.Text
      && body.childAt(1).getContent().equalsIgnoreCase("-" + STYLE)
      && body.childAt(2).getType() == SymbolType.Whitespace
      && body.childAt(3).getType() == SymbolType.Text) {
      current.putProperty(STYLE, body.childAt(3).getContent());
    }
    return new Maybe<>(current);
  }

  @Override
  public String toTarget(Translator translator, Symbol current) {
    String style = current.findProperty(STYLE, DEFAULT_STYLE);
    final List<Symbol> headerLines = findHeaderLines(((HtmlTranslator)translator).getSyntaxTree());
    HeadingContentBuilder headingContentBuilder = new HeadingContentBuilder(headerLines,
      LIST_STYLES.contains(style) ? style :DEFAULT_STYLE);
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
  private static final List<String> LIST_STYLES = new ArrayList<>(Arrays.asList(
    "decimal", "decimal-leading-zero", "lower-roman", "upper-roman", "lower-alpha", "upper-alpha", "none"));

  static String extractTextFromHeaderLine(final Symbol headerLine) {
    final StringBuilder sb = new StringBuilder();
    headerLine.walkPreOrder(node -> {
      if (node.isType(SymbolType.Text) || node.isType(Literal.symbolType) || node.isType(Whitespace)) {
        sb.append(node.getContent());
      }
    });
    return sb.toString();
  }

  class HeadingContentBuilder {

    private final List<Symbol> headerLines;
    private final Stack<HtmlTag> stack = new Stack<>();
    private final String listStyle;
    private final HtmlTag rootElement;
    private boolean processed;

    HeadingContentBuilder(final List<Symbol> headerLines, final String listStyle) {
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
        listElement.addAttribute("style", "list-style-type: " + listStyle + ";");
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
        headerLine.findProperty(Names.ID).ifPresent(id -> anchorElement.addAttribute("href", "#" + id));
        listitemElement.add(anchorElement);
        stack.peek().add(listitemElement);
        processed = true;
      }
    }

    private int currentLevel() {
      return stack.size() - 1;
    }

    private int getLevel(final Symbol headerLine) {
      return Integer.parseInt(headerLine.findProperty(Names.LEVEL, "0"));
    }

  }

}
