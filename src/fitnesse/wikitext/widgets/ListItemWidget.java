// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

public class ListItemWidget extends ParentWidget {
  private int level;

  public ListItemWidget(ParentWidget parent, String text, int level) throws Exception {
    super(parent);
    this.level = level;
    addChildWidgets(text);
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer();
    for (int i = 0; i < level; i++)
      html.append("\t");
    html.append("<li>").append(childHtml()).append("</li>\n");

    return html.toString();
  }
}
