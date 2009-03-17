// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WikiWidget;

public class VirtualWikiWidget extends WikiWidget {
  public static final String REGEXP = "^!virtualwiki http://[^\r\n]*";
  private static final Pattern pattern = Pattern.compile("!virtualwiki (http://.*)");

  public String url;

  public VirtualWikiWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      url = match.group(1);
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("");
    html.append("<span class=\"meta\">");
    html.append("!virtualwiki has been deprecated.  Use the Properties button instead.");
    html.append("</span>");
    return html.toString();
  }

  public String getRemoteUrl() {
    return url;
  }
}
