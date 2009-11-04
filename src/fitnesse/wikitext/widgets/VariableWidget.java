// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableWidget extends ParentWidget {
  public static final String REGEXP = "\\$\\{[\\w\\.]+\\}";
  public static final Pattern pattern = Pattern.compile("\\$\\{([\\w\\.]+)\\}", Pattern.MULTILINE + Pattern.DOTALL);

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
    //Todo: The nonlocalities in this function are truly horrendous.  There is a loop in the widget builder that
    //reapplies the variable widgets until all the variables have been replaced.  This function is part of the
    //body of that loop.  The parent.addVariable statement ensures that the value of a variable is updated
    //each time through the loop which makes sure that nested variables are expressed properly.
    //The if (matcher.find()) statement ensures that if there are no more nested variables to express, we don't
    //continue to change the value of the variable.  If you don't understand that, well, I'm not sure I do either.
    //The whole variable mechanism needs to be completely redone...

    String value = parent.getVariable(name);
    if (value != null) {
      Matcher matcher = pattern.matcher(value);
      addChildWidgets(value);
      renderedText = childHtml();
      if (matcher.find())
        parent.addVariable(name, renderedText);
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


