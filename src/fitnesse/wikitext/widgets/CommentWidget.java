// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

public class CommentWidget extends TextWidget {
  //A comment begins with a # to the end of line.
  //But there is a special case.  If there is a # followed by a table row, then that is not a comment.
  // i.e. #|row|  is not a comment.  And the table widget will parse it as a hidden-first-row table.
  //However, we want people to be able to comment out tables.  Therefore:
  //#|row|
  //#|row|
  // ...must be interpreted as a comment and not a hidden-first-row-table.
  //
  //Remember that tables can also begin with ! so #!|row| is a hidden-first-row table and not a comment.
  //
  // This is really awful.  We need a better way to parse widgets.

  public static final String REGEXP =
    "^#(\n|(?:[^!\\|\n])[^\n]*\n?|!?\\|[^\n]*[^\\|]\n|[^\n]*\n#[^\n]*\n)";

  public CommentWidget(ParentWidget parent, String text) {
    super(parent, text);
  }

  public String render() throws Exception {
    return "";
  }
}

