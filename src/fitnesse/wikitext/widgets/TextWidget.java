// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.Utils;
import fitnesse.wikitext.WikiWidget;

public class TextWidget extends WikiWidget implements WidgetWithTextArgument {
  private String text;

  public TextWidget(ParentWidget parent) {
    super(parent);
  }

  public TextWidget(ParentWidget parent, String text) {
    super(parent);
    this.setText(text);
  }

  public String getText() {
    return Utils.unescapeWiki(text);
  }

  public void setText(String newText) {
    text = newText;
  }

  public String render() throws Exception {
    String html = getText();
    if (parent.doEscaping())
      html = Utils.escapeHTML(html);
    html = html.replaceAll("\r\n", "\n");
    html = html.replaceAll("\r", "\n");
    html = html.replaceAll("\n", "<br/>");

    return html;
  }

  public String toString() {
    return super.toString() + " : " + getText();
  }

  public String asWikiText() throws Exception {
    return text;
  }

  public String getRawText() {
    return text;
  }
}
