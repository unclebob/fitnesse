// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.responders.ChunkingResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;

public abstract class ResultResponder extends ChunkingResponder implements
  SecureResponder, Traverser<Object> {

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  protected void doSending() {
    HtmlPage htmlPage = context.pageFactory.newPage();
    htmlPage.setTitle(getTitle());
    htmlPage.setPageTitle(new PageTitle(getTitle()) {
      public String getTitle() {
        return "search";
      }

      public String getLink() {
        return null;
      }
    });
    htmlPage.setMainTemplate("searchResults");

    if (page == null)
      page = context.getRootPage().getPageCrawler().getPage(PathParser.parse("FrontPage"));
    if (request.getQueryString() == null || request.getQueryString().equals(""))
      htmlPage.put("request", request.getBody());
    else
      htmlPage.put("request", request.getQueryString());
    htmlPage.put("page", page);
    htmlPage.put("viewLocation", request.getResource());
    htmlPage.setNavTemplate("viewNav");
    htmlPage.put("resultResponder", this);
    
    htmlPage.render(response.getWriter());
    
    response.closeAll();
  }

  protected abstract String getTitle() ;

  public abstract void traverse(TraversalListener<Object> observer);

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
  
}


