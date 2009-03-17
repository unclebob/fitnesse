// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WidgetBuilder;

public class PreformattedWidget extends ParentWidget {
  public static final String REGEXP = "\\{\\{\\{.+?\\}\\}\\}";
  private static final Pattern pattern = Pattern.compile("\\{{3}(.+?)\\}{3}", Pattern.DOTALL);

  public PreformattedWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      addChildWidgets(match.group(1));
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<pre>");
    html.append(childHtml()).append("</pre>");

    return html.toString();
  }

  public String asWikiText() throws Exception {
    return "{{{" + childWikiText() + "}}}";
  }

  public WidgetBuilder getBuilder() {
    return WidgetBuilder.literalVariableEvaluatorWidgetBuilder;
  }
}

