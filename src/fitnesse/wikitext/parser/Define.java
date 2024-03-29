package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;

public class Define extends SymbolType implements Rule, Translation {
  public Define() {
    super("Define");
    wikiMatcher(new Matcher().startLineOrCell().string("!define"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

    Maybe<String> name = parser.parseToAsString(SymbolType.Whitespace);
    if (name.isNothing()) return Symbol.nothing;
    String variableName = name.getValue();
    if (!ScanString.isVariableName(variableName)) return Symbol.nothing;

    Symbol next = parser.moveNext(1);
    Maybe<String> valueString = (next.isType(SymbolType.Text))
      ? copyVariableValue(parser, next)
      : parseVariableValue(parser, next);
    if (valueString.isNothing()) return Symbol.nothing;

    String variableValue = valueString.getValue();
    parser.getPage().putVariable(variableName, variableValue);
    return new Maybe<>(current.add(variableName).add(variableValue));
  }

  private Maybe<String> copyVariableValue(Parser parser, Symbol next) {
    String fromVariableName = next.getContent();
    if (!ScanString.isVariableName(fromVariableName)) return Maybe.noString;
    return parser.getVariableSource().findVariable(fromVariableName)
      .map(Maybe::new)
      .orElse(Maybe.noString);
  }

  private Maybe<String> parseVariableValue(Parser parser, Symbol next) {
    SymbolType nextType = next.getType();

    // for backward compatibility: !define x<eof> is valid
    if (nextType == SymbolType.Empty) return new Maybe<>("");

    if (nextType == SymbolType.OpenBrace) return parser.parseToAsString(SymbolType.CloseBrace);
    if (nextType == SymbolType.OpenBracket) return parser.parseToAsString(SymbolType.CloseBracket);
    if (nextType == SymbolType.OpenParenthesis) return parser.parseToAsString(SymbolType.CloseParenthesis);
    
    return Maybe.noString;
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    HtmlTag result = new HtmlTag("span", "variable defined: "
      + translator.translate(symbol.childAt(0))
      + "="
      + translator.translate(symbol.childAt(1)));
    result.addAttribute("class", "meta");
    return result.html();
  }

}
