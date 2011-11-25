// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XRefWidget extends ParentWidget implements WidgetWithTextArgument {
  public static final String REGEXP = "^!see " + WikiWordWidget.REGEXP;
  private static final Pattern pattern = Pattern.compile("^!see (.*)");
  private String pageName;

  public XRefWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      pageName = match.group(1);
      addChildWidgets(pageName);
    }
  }

  public String render() {
    return HtmlUtil.metaText("<b>See: " + childHtml() + "</b>");
  }

  public String asWikiText() {
    return "!see " + pageName;
  }

  public String getText() {
    return pageName;
  }
}
