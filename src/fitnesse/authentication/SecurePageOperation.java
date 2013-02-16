// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import fitnesse.wiki.*;

import java.util.Iterator;
import java.util.List;

public abstract class SecurePageOperation implements SecureOperation {
  protected abstract String getSecurityMode();

  public boolean shouldAuthenticate(FitNesseContext context, Request request) throws Exception {
    WikiPagePath path = PathParser.parse(request.getResource());
    PageCrawler crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new MockingPageCrawler());
    WikiPage page = crawler.getPage(context.root, path);
    if (page == null)
      return false;

    List<WikiPage> ancestors = WikiPageUtil.getAncestorsStartingWith(page);
    for (Iterator<WikiPage> iterator = ancestors.iterator(); iterator.hasNext();) {
      WikiPage ancestor = iterator.next();
      if (hasSecurityModeAttribute(ancestor))
        return true;
    }
    return false;
  }

  private boolean hasSecurityModeAttribute(WikiPage ancestor) throws Exception {
    PageData data = ancestor.getData();
    boolean hasSecurityMode = data.hasAttribute(getSecurityMode());
    return hasSecurityMode;
  }
}
