// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SearchObserver;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public abstract class ResultResponder extends ChunkingResponder implements
  SecureResponder {
  private BlockingQueue queue = new ArrayBlockingQueue<WikiPage>(64);

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  protected void doSending() {
    HtmlPage htmlPage = context.htmlPageFactory.newPage();
    htmlPage.setTitle(getTitle());
    htmlPage.setPageTitle(new PageTitle(getTitle()) {
      public String getTitle() {
        return "search";
      }

      public String getLink() {
        return "search";
      }
    });
    htmlPage.setMainTemplate("searchResults");

    if (page == null)
      page = context.root.getPageCrawler().getPage(context.root, PathParser.parse("FrontPage"));
    if (request.getQueryString() == null || request.getQueryString().equals(""))
      htmlPage.put("request", request.getBody());
    else
      htmlPage.put("request", request.getQueryString());
    htmlPage.put("page", page);

    htmlPage.put("resultResponder", this);
    
    // TODO: use Directive -- htmlPage.put("searchIterator", new SearchIterator(this));

    htmlPage.render(response.getWriter());
    
    response.closeAll();
  }

  protected abstract String getTitle() ;

  protected abstract void startSearching(SearchObserver observer);

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
  
}


