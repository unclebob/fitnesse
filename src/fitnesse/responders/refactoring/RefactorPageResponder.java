// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RefactorPageResponder implements SecureResponder {

  public Response makeResponse(FitNesseContext context, Request request) {
    String resource = request.getResource();

    String tags = "";
    if(context.getRootPage() != null){
      WikiPagePath path = PathParser.parse(resource);
      WikiPage wikiPage = context.getRootPage().getPageCrawler().getPage(path);
      if(wikiPage != null) {
        PageData pageData = wikiPage.getData();
        tags = pageData.getAttribute(PageData.PropertySUITES);
      }
    }
    
    HtmlPage page = context.pageFactory.newPage();

    page.setMainTemplate("refactorForm");
    page.setTitle("Refactor: " + resource);
    page.setPageTitle(new PageTitle("Refactor", PathParser.parse(resource), tags));
    page.put("refactoredRootPage", resource);
    page.put("request", request);
    page.put("type", request.getInput("type"));
    page.put("viewLocation", request.getResource());
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html());
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
