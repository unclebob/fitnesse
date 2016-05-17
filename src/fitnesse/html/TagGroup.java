// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

public class TagGroup extends HtmlTag {
  public TagGroup() {
    super("group");
  }

  @Override
  public String html(int depth) {
    StringBuilder buffer = new StringBuilder();
    for (HtmlElement element : this) {
      if (element instanceof HtmlTag)
        buffer.append(((HtmlTag) element).html(depth));
      else
        buffer.append(element.html());
    }
    return buffer.toString();
  }
}
