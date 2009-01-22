// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleWidget extends ParentWidget {
  private String style = "TILT";

  public StyleWidget(ParentWidget parent) {
    super(parent);
  }

  protected void buildWidget(Pattern matchingPattern, String text) throws Exception {
    Matcher match = matchingPattern.matcher(text);
    if (match.find()) {
      style = match.group(1);
      addChildWidgets(match.group(2));
    }
  }

  public String render() throws Exception {
    return String.format("<span class=\"%s\">%s</span>", style, childHtml());
  }

  public static class ParenFormat extends StyleWidget {
    public static final String REGEXP = "!style_\\w+\\([^\r\n\\)]*\\)";
    private static final Pattern pattern = Pattern.compile("!style_(\\w+)\\(([^\\)]*)\\)");

    public ParenFormat(ParentWidget parent, String text) throws Exception {
      super(parent);
      buildWidget(pattern, text);
    }

  }

  public static class BracketFormat extends StyleWidget {
    public static final String REGEXP = "!style_\\w+\\[[^\r\n\\]]*\\]";
    private static final Pattern pattern = Pattern.compile("!style_(\\w+)\\[([^\\]]*)\\]");

    public BracketFormat(ParentWidget parent, String text) throws Exception {
      super(parent);
      buildWidget(pattern, text);
    }
  }

  public static class BraceFormat extends StyleWidget {
    public static final String REGEXP = "!style_\\w+\\{[^\r\n\\}]*\\}";
    private static final Pattern pattern = Pattern.compile("!style_(\\w+)\\{([^\\}]*)\\}");

    public BraceFormat(ParentWidget parent, String text) throws Exception {
      super(parent);
      buildWidget(pattern, text);
    }
  }

}
