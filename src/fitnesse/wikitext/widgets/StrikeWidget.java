// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// created by Jason Sypher

public class StrikeWidget extends ParentWidget {
  public static final String REGEXP = "--(?:[^-].+?)--";
  private static final Pattern pattern = Pattern.compile("--(.+?)--", Pattern.MULTILINE + Pattern.DOTALL);

// The following regexp is intersting becuase each addition char
// in the string to match would double the time it took to parse.
//	public static final String REGEXP = "--(?:(?:[^-]+[-]?[^-]+)+)--";

  public StrikeWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      addChildWidgets(match.group(1));
  }

  public String render() throws Exception {
    StringBuffer strike = new StringBuffer("<span class=\"strike\">");
    strike.append(childHtml()).append("</span>");
    return strike.toString();

  }
}
