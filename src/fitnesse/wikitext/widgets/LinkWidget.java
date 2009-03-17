// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WidgetBuilder;

public class LinkWidget extends ParentWidget {
  public static final String REGEXP = "https?://[^\\s]+[^\\s.)]+";
  private static final Pattern pattern = Pattern.compile("https?://([^/\\s]*)(\\S*)?");

  public LinkWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    addChildWidgets(text);
  }

  public String render() throws Exception {
    String linkText = childHtml();
    String usableURL = makeUrlUsable(linkText);
    StringBuffer html = new StringBuffer("<a href=\"");
    html.append(usableURL);
    html.append("\">");
    html.append(linkText);
    html.append("</a>");

    return html.toString();
  }

  public static String makeUrlUsable(String url) {
    String usableUrl = url;
    Matcher match = pattern.matcher(url);
    if (match.find()) {
      String host = match.group(1);
      String resource = match.group(2);
      if ("files".equals(host))
        usableUrl = "/files" + resource;
    }

    return usableUrl;
  }

  public WidgetBuilder getBuilder() {
    return WidgetBuilder.variableEvaluatorWidgetBuilder;
  }

  public String asWikiText() throws Exception {
    return childWikiText();
  }
}


