// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CenterWidget extends ParentWidget {
  public static final String REGEXP = "^![cC] [^\r\n]*" + LINE_BREAK_PATTERN + "?";
  private static final Pattern pattern = Pattern.compile("^![cC] (.*)");

  public CenterWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      addChildWidgets(match.group(1));
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<div class=\"centered\">");
    html.append(childHtml()).append("</div>");
    return html.toString();
  }  
}
