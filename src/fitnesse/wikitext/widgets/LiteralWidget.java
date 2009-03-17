// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WikiWidget;

public class LiteralWidget extends WikiWidget {
  public static final String REGEXP = "!lit\\?\\d+\\?";
  public static final Pattern pattern = Pattern.compile("!lit\\?(\\d+)\\?", Pattern.MULTILINE + Pattern.DOTALL);
  private int literalNumber;

  public LiteralWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      literalNumber = Integer.parseInt(match.group(1));
    }
  }

  public String render() throws Exception {
    return parent.getLiteral(literalNumber);
  }

  public String asWikiText() throws Exception {
    return "!-" + parent.getLiteral(literalNumber) + "-!";
  }
}

