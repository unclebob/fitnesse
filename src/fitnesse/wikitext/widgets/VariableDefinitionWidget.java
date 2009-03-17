// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.html.HtmlUtil;

public class VariableDefinitionWidget extends ParentWidget {
  public static final String REGEXP = "^!define [\\w\\.]+ +(?:(?:\\{[^}]*\\})|(?:\\([^)]*\\)))";
  private static final Pattern pattern =
    Pattern.compile("^!define ([\\w\\.]+) +([\\{\\(])(.*)[\\}\\)]",
      Pattern.DOTALL + Pattern.MULTILINE);

  public String name;
  public String value;

  public VariableDefinitionWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      name = match.group(1);
      value = match.group(3);
    }
  }

  public String render() throws Exception {
    this.parent.addVariable(name, value);
    return HtmlUtil.metaText("variable defined: " + name + "=" + value);
  }

  public String asWikiText() throws Exception {
    String text = "!define " + name + " ";
    if (value.indexOf("{") == -1)
      text += "{" + value + "}";
    else
      text += "(" + value + ")";
    return text;
  }
}
