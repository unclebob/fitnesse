// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.responders.run.HtmlFormatterClient;

public class HtmlComment extends HtmlTag {
  public String comment;

  public HtmlComment(String comment) {
    super("comment");
    this.comment = comment;
  }

  public String html(int depth) {
    return makeIndent(depth) + "<!--"+ comment+"-->"+endl;
  }

}
