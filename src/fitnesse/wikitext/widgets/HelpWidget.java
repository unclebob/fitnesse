// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.WikiWidget;

//created by Clare McLennan

public class HelpWidget extends WikiWidget {
  public static final String REGEXP = "!help *(-editable)?";
  private final boolean editable;

  public HelpWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    editable = text.contains("-editable");
  }

  public String render() throws Exception {
    String helpText = getWikiPage().getHelpText();
    String editString = "edit";
    if (helpText == null) {
      helpText = "";
      editString = "edit help text";
    }


    WikiPagePath path = getWikiPage().getPageCrawler().getFullPath(getWikiPage());
    if (editable) {
      helpText += " <a href=\"" + path.toString() + "?properties\">(" + editString + ")</a>";
    }

    return helpText;
  }
}
