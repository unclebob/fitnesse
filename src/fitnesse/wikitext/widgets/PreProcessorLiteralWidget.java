// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.Utils;
import fitnesse.wikitext.WikiWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessorLiteralWidget extends WikiWidget {
  public static final String REGEXP = "![<-].*?[>-]!";
  public static final Pattern pattern = Pattern.compile("![<-](.*?)[>-]!", Pattern.MULTILINE + Pattern.DOTALL);
  private String literalToRender = null;
  private int literalNumber;
  private boolean escapedLiteral = false;
  private String literalText;

  public PreProcessorLiteralWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      if (match.group(0).charAt(1) == '<')
        escapedLiteral = true;
      literalText = match.group(1);
      literalToRender = escapedLiteral ? Utils.escapeHTML(literalText) : literalText;
      literalNumber = this.parent.defineLiteral(literalToRender);
    }
  }

  public String render() throws Exception {
    return "!lit?" + literalNumber + "?";
  }

  public String asWikiText() throws Exception {
    if (escapedLiteral)
      return "!<" + literalText + ">!";
    else
      return "!-" + literalToRender + "-!";
  }
}
