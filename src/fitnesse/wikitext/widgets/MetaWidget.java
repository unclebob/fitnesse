// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaWidget extends ParentWidget {
  public static final String REGEXP = "^!meta [^\r\n]*";
  private static final Pattern pattern = Pattern.compile("^!meta (.*)");

  private String content;

  public MetaWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      setContent(match.group(1));
  }

  private void setContent(String content) {
    this.content = content;
    addChildWidgets(this.content);
  }

  public String render() {
    return HtmlUtil.metaText(childHtml());
  }

  public String asWikiText() {
    return "!meta " + content;
  }
}
