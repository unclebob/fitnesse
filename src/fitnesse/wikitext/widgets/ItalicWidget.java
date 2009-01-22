// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItalicWidget extends ParentWidget {
  public static final String REGEXP = "''.+?''";
  private static final Pattern pattern = Pattern.compile("''(.+?)''", Pattern.MULTILINE + Pattern.DOTALL);

  public ItalicWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      addChildWidgets(match.group(1));
    else
      System.err.println("ItalicWidget: match was not found");
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<i>");
    html.append(childHtml()).append("</i>");

    return html.toString();
  }

}

