// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.parser;

import java.util.Random;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Matcher;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.Rule;
import fitnesse.wikitext.parser.ScanString;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Translation;
import fitnesse.wikitext.parser.Translator;

public class RandomNumber extends SymbolType implements Rule, Translation
{
  public RandomNumber()
  {
    super("RandomNumber");
    wikiMatcher(new Matcher().string("!randomNumber"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public String toTarget(final Translator translator, final Symbol symbol)
  {
    final HtmlTag result = new HtmlTag("span", "random number defined: " + translator.translate(symbol.childAt(0)) + "="
        + translator.translate(symbol.childAt(1)));
    result.addAttribute("class", "meta");
    return result.html();
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @see fitnesse.wikitext.parser.Rule#parse(fitnesse.wikitext.parser.Symbol, fitnesse.wikitext.parser.Parser)
   */
  @Override
  public Maybe<Symbol> parse(final Symbol symbol, final Parser parser)
  {
    String _value = "Invalid range! Use: !randomNumber (min max)";
    if (!parser.isMoveNext(SymbolType.Whitespace))
      return Symbol.nothing;

    final Maybe<String> name = parser.parseToAsString(SymbolType.Whitespace);
    if (parser.atEnd())
      return Symbol.nothing;
    if (!ScanString.isVariableName(name.getValue()))
      return Symbol.nothing;

    final Symbol next = parser.moveNext(1);
    final SymbolType close = next.closeType();
    if (close == SymbolType.Empty)
      return Symbol.nothing;

    try
    {
      final Maybe<String> _min = parser.parseToAsString(SymbolType.Whitespace);

      final Maybe<String> _max = parser.parseToAsString(close);

      final Random _rand = new Random();
      _value = Integer.toString(_rand.nextInt((Integer.parseInt(_max.getValue()) - Integer.parseInt(_min.getValue())) + 1) + Integer.parseInt(_min.getValue()));
    }
    catch (final Exception e)
    {
      // do nothing
      e.printStackTrace();
    }

    parser.getPage().putVariable(name.getValue(), _value);

    return new Maybe<Symbol>(symbol.add(name.getValue()).add(_value));
  }
}