// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

public class CommentWidget extends TextWidget {
  public static final String REGEXP = "^#(" + LINE_BREAK_PATTERN + "|(?:[^!\\|\r\n])[^\r\n]*" + LINE_BREAK_PATTERN + "?|!?\\|[^\r\n]*[^\\|]" + LINE_BREAK_PATTERN + ")";

  public CommentWidget(ParentWidget parent, String text) {
    super(parent, text);
  }

  public String render() throws Exception {
    return "";
  }
}

