// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableWidget extends ParentWidget {
  public static final String prefix = "\\$\\{";
  public static final String REGEXP = prefix + "[\\w\\.]+\\}";
  public static final Pattern pattern = Pattern.compile(prefix + "([\\w\\.]+)\\}", Pattern.MULTILINE + Pattern.DOTALL);
  public static final String prefixDisabled = "!\\{";
  
  private String name = null;
  private String renderedText;
  private boolean rendered;

  public VariableWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      name = match.group(1);
    }
  }

  public String render() throws Exception {
    if (!rendered)
      doRender();
    return renderedText;
  }

  private void doRender() throws Exception {
    String value = parent.getVariable(name);
    if (value != null) {
      addChildWidgets(value);
      renderedText = childHtml();
    } else
      renderedText = makeUndefinedVariableExpression(name);
    rendered = true;
  }

  private String makeUndefinedVariableExpression(String name) throws Exception {
    return HtmlUtil.metaText("undefined variable: " + name);
  }

  public String asWikiText() throws Exception {
    return "${" + name + "}";
  }
}


