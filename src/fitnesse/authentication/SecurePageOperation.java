// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.components.TraversalListener;
import fitnesse.http.Request;
import fitnesse.wiki.*;

public abstract class SecurePageOperation implements SecureOperation {
  protected abstract String getSecurityMode();

  public boolean shouldAuthenticate(FitNesseContext context, Request request) {
    WikiPagePath path = PathParser.parse(request.getResource());
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    WikiPage page = crawler.getPage(path, new MockingPageCrawler());
    if (page == null)
      return false;

    final boolean[] found = new boolean[1];
    page.getPageCrawler().traversePageAndAncestors(new TraversalListener<WikiPage>() {
      @Override
      public void process(WikiPage page) {
        if (hasSecurityModeAttribute(page))
          found[0] = true;
      }
    });
    return found[0];
  }

  private boolean hasSecurityModeAttribute(WikiPage ancestor) {
    PageData data = ancestor.getData();
    return data.hasAttribute(getSecurityMode());
  }
}
