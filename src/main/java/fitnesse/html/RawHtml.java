// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

public class RawHtml extends HtmlElement {
  private String html;

  public RawHtml(String html) {
    this.html = html;
  }

  public String html() {
    return html;
  }
}
