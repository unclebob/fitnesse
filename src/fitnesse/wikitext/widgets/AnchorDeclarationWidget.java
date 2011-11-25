// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.WikiWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnchorDeclarationWidget extends WikiWidget {

  public static final String REGEXP = "!anchor \\w+";
  private static final Pattern pattern = Pattern.compile("!anchor (\\w*)");

  private String text, anchorName;

  public AnchorDeclarationWidget(ParentWidget parent, String text) {
    super(parent);
    this.text = text;
    Matcher match = pattern.matcher(this.text);
    if (match.find())
      anchorName = match.group(1);
  }

  public String render() {
    return HtmlUtil.makeAnchorTag(anchorName).html();
  }
}
