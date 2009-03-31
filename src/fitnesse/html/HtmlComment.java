// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

public class HtmlComment extends HtmlTag {
  public String comment;

  public HtmlComment(String comment) {
    super("commant");
    this.comment = comment;
  }

  public String html(int depth) {
    StringBuffer buffer = new StringBuffer();
    addTabs(depth, buffer);
    buffer.append("<!--").append(comment).append("-->").append(endl);
    return buffer.toString();
  }
}
