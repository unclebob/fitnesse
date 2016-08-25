package fitnesse.wikitext.parser;

import fitnesse.util.StringUtils;

import static fitnesse.util.StringUtils.isBlank;

public class FrontMatter extends SymbolType implements Rule, Translation {
  public static final FrontMatter symbolType = new FrontMatter();
  public static final SymbolType keyValueSymbolType = new SymbolType("KeyValue");

  private static final String FRONT_MATTER_DELIMITER = "---\n";

  private static SymbolProvider SYMBOL_PROVIDER = new SymbolProvider(new SymbolType[] {
    CloseFrontMatter.symbolType, SymbolType.Colon, SymbolType.Whitespace, SymbolType.Newline, SymbolType.Text
  });

  FrontMatter() {
    super("FrontMatter");
    wikiMatcher(new Matcher().startLine().string(FRONT_MATTER_DELIMITER));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    if (current.getStartOffset() != 0) return Symbol.nothing;

    final Symbol frontMatter = parser.parseToWithSymbols(CloseFrontMatter.symbolType, SYMBOL_PROVIDER, 0);

    if (!parser.getCurrent().isType(CloseFrontMatter.symbolType)) return Symbol.nothing;

    Maybe<Symbol> yaml = processYaml(current, frontMatter);
    if (yaml.isNothing()) {
      return Symbol.nothing;
    }
    return new Maybe<>(current);
  }

  private Maybe<Symbol> processYaml(Symbol yaml, Symbol symbolList) {
    boolean addToPrevious = false;
    String key = null, value = "";
    for (Symbol symbol : symbolList.getChildren()) {
      if (symbol.isType(SymbolType.Whitespace) && key == null) {
        addToPrevious = true;
      } else if (symbol.isType(SymbolType.Text) && key == null) {
        key = symbol.getContent();
      } else if (symbol.isType(SymbolType.Text) || symbol.isType(SymbolType.Whitespace) || (symbol.isType(SymbolType.Colon) && !isBlank(value))) {
        value += symbol.getContent();
      } else if (symbol.isType(SymbolType.Colon)) {
        // Now start filling value
        if (key == null) return Symbol.nothing;
      } else if (symbol.isType(SymbolType.Newline)) {
        if (key != null) {
          if (addToPrevious)
            yaml.getChildren().get(yaml.getChildren().size() - 1).add(yamlLine(key, value.trim()));
          else
            yaml.add(yamlLine(key, value.trim()));
        }
        key = null;
        value = "";
        addToPrevious = false;
      } else {
        // All possible alternatives should be covered now.
        return Symbol.nothing;
      }
    }
    return new Maybe<>(yaml);
  }

  private Symbol yamlLine(final String key, final String value) {
    return new Symbol(keyValueSymbolType).add(key).add(value);
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    return "";
  }

  private static class CloseFrontMatter extends SymbolType {
    private static final CloseFrontMatter symbolType = new CloseFrontMatter();

    private CloseFrontMatter() {
      super("EndOfFrontMatter");
      wikiMatcher(new Matcher().startLine().string(FRONT_MATTER_DELIMITER));
    }

  }

}
