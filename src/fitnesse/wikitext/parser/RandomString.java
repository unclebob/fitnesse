// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;

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

public class RandomString extends SymbolType implements Rule, Translation
{
  public RandomString()
  {
    super("RandomString");
    wikiMatcher(new Matcher().string("!randomString"));
    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public String toTarget(final Translator translator, final Symbol symbol)
  {
    final HtmlTag result = new HtmlTag("span", "random string defined: " + translator.translate(symbol.childAt(0)) + "="
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
    String _value = "Invalid random string! Use: !randomString (min max a-z,A-Z,3-8,ä,ö,Ü)";
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

      final Maybe<String> _max = parser.parseToAsString(SymbolType.Whitespace);

      final Random _rand = new Random();
      final int _randomLength = _rand.nextInt((Integer.parseInt(_max.getValue()) - Integer.parseInt(_min.getValue())) + 1) + Integer.parseInt(_min.getValue());

      final Maybe<String> _regex = parser.parseToAsString(close);

      final String[] _group = _regex.getValue().split(",");

      _value = generateString(_randomLength, Arrays.asList(_group));
    }
    catch (final Exception e)
    {
      // do nothing
      parser.parseToAsString(close);
    }

    parser.getPage().putVariable(name.getValue(), _value);

    return new Maybe<Symbol>(symbol.add(name.getValue()).add(_value));
  }

  private String generateString(final int randomLength, final List<String> regexList)
  {
    char[] _chars = new char[0];

    if (regexList != null)
    {
      for (final String _regex : regexList)
      {
        if (_regex.contains("-"))
        {
          final String[] _fromToChars = _regex.split("-");
          final char[] _addChars = generateCharArray(_fromToChars[0], _fromToChars[1]);
          _chars = ArrayUtils.addAll(_chars, _addChars);
        }
        else
        {
          final char[] _addChars = _regex.toCharArray();
          _chars = ArrayUtils.addAll(_chars, _addChars);
        }
      }
    }

    return RandomStringUtils.random(randomLength, _chars);
  }

  private char[] generateCharArray(final String fromString, final String toString)
  {
    final char _fromChar = fromString.toCharArray()[0];
    final char _toChar = toString.toCharArray()[0];

    final char[] _chars = new char[(_toChar - _fromChar) + 1];

    int _arrIndex = 0;
    for (int _index = _fromChar; _index < _toChar + 1; _index++)
    {
      _chars[_arrIndex++] = (char)_index;
    }
    return _chars;
  }
}