// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.wiki.WikiPage;
import org.json.JSONArray;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NameWikiPageResponder extends BasicWikiPageResponder {
  protected String contentFrom(WikiPage requestedPage)
    throws Exception {
    List<String> pages = new ArrayList<String>();
    for (Iterator<?> iterator = requestedPage.getChildren().iterator(); iterator.hasNext();) {
      WikiPage child = (WikiPage) iterator.next();
      if (request.hasInput("ShowChildCount")) {
        String name = child.getName() + " " + Integer.toString(child.getChildren().size());
        pages.add(name);
      } else
        pages.add(child.getName());

    }

    String format = (String) request.getInput("format");
    if ("json".equalsIgnoreCase(format)) {
      JSONArray jsonPages = new JSONArray(pages);
      return jsonPages.toString();
    }
    return StringUtil.join(pages, System.getProperty("line.separator"));
  }

  protected String getContentType() {
    return "text/plain";
  }
}
