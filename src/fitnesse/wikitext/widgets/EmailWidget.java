// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
// Copyright (C) 2003,2004 by Robert C. Martin and Micah D. Martin. All rights reserved.
// Released under the terms of the GNU  General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

public class EmailWidget extends WikiWidget {

  public static final String REGEXP = "[\\w-_.]+@[\\w-_.]+\\.[\\w-_.]+";

  private String emailAddress;

  public EmailWidget(ParentWidget parent, String text) {
    super(parent);
    emailAddress = text;
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<a href=\"mailto:");
    html.append(emailAddress);
    html.append("\">");
    html.append(emailAddress);
    html.append("</a>");

    return html.toString();
  }

  public String asWikiText() {
    return emailAddress;
  }
}
