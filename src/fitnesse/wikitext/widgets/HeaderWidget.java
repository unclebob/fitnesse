// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderWidget extends ParentWidget {
  public static final String REGEXP = "^![1-6] [^\r\n]*(?:(?:\r\n)|\n|\r)?";
  private static final Pattern pattern = Pattern.compile("!([1-6]) (.*)");

  private int size = 3;

  public HeaderWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      size = Integer.valueOf(match.group(1)).intValue();
      addChildWidgets(match.group(2).trim());
    }
  }

  public int size() {
    return size;
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<h");
    html.append(size).append(">").append(childHtml());
    html.append("</h").append(size).append(">");

    return html.toString();
  }
}
