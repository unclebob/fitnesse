// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.wiki.WikiPage;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import org.json.JSONArray;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NameWikiPageResponder extends BasicWikiPageResponder {
  protected String contentFrom(WikiPage requestedPage) {
    List<String> lines = addLines(requestedPage);

    String format = (String) request.getInput("format");
    if ("json".equalsIgnoreCase(format)) {
      JSONArray jsonPages = new JSONArray(lines);
      return jsonPages.toString();
    }
    return StringUtil.join(lines, System.getProperty("line.separator"));
  }

  private List<String> addLines(WikiPage requestedPage) {
    List<String> lines = new ArrayList<String>();
    for (Iterator<?> iterator = requestedPage.getChildren().iterator(); iterator.hasNext();) {
      WikiPage child = (WikiPage) iterator.next();
      lines.add(makeLine(child));
    }
    return lines;
  }

  private String makeLine(WikiPage child) {
    String line;
    if (request.hasInput("ShowChildCount")) {
      int numberOfChildren = child.getChildren().size();
      line = child.getName() + " " + numberOfChildren;
    } else
      line = child.getName();
    return line;
  }

  protected String getContentType() {
    return "text/plain";
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
