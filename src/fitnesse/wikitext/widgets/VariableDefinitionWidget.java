// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableDefinitionWidget extends ParentWidget {
  public static final String REGEXP = "^!define [\\w\\.]+ +(?:(?:\\{[^}]*\\})|(?:\\([^)]*\\))|(?:\\[[^]]*\\]))";
  private static final Pattern pattern =
    Pattern.compile("^!define ([\\w\\.]+) +([\\{\\(\\[])(.*)[\\}\\)\\]]",
      Pattern.DOTALL + Pattern.MULTILINE);

  public String name;
  public String value;
  public String bracket;

  public VariableDefinitionWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      name = match.group(1);
      bracket = match.group(2);
      value = match.group(3);
    }
  }

  public String render() throws Exception {
    this.parent.addVariable(name, value);
    return HtmlUtil.metaText("variable defined: " + name + "=" + value);
  }

  public String asWikiText() throws Exception {
    String text = "!define " + name + " ";
    if (bracket.equals("{"))
      text += "{" + value + "}";
    else if (bracket.equals("("))
      text += "(" + value + ")";
    else
      text += "[" + value + "]";
    return text;
  }
}
