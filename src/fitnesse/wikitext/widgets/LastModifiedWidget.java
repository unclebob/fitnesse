// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WikiWidget;

//created by Jason Sypher

public class LastModifiedWidget extends WikiWidget {
  public static final String REGEXP = "^!lastmodified";

  private static SimpleDateFormat makeTimeFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("hh:mm:ss a");
  }

  private static SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("MMM dd, yyyy");
  }

  public LastModifiedWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
  }

  public String render() throws Exception {
    PageData data = getWikiPage().getData();
    String formattedDate = formatDate(data.getProperties().getLastModificationTime());
    String user = data.getAttribute(WikiPage.LAST_MODIFYING_USER);
    if (user == null || "".equals(user))
      return HtmlUtil.metaText("Last modified anonymously on " + formattedDate);
    else
      return HtmlUtil.metaText("Last modified by " + user + " on " + formattedDate);
  }

  public static String formatDate(Date date) {
    String formattedDate = makeDateFormat().format(date) + " at " + makeTimeFormat().format(date);

    return formattedDate;
  }

}
