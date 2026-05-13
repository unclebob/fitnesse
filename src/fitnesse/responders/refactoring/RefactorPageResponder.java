// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.util.StringUtils;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;

public class RefactorPageResponder implements SecureResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String resource = request.getResource();
    String tags = "";
    WikiPage wikiPage = null;
    if(context.getRootPage() != null){
      WikiPagePath path = PathParser.parse(resource);
      wikiPage = context.getRootPage().getPageCrawler().getPage(path);
      if(wikiPage != null) {
        PageData pageData = wikiPage.getData();
        tags = pageData.getAttribute(WikiPageProperty.SUITES);
      }
    }

    HtmlPage page = context.pageFactory.newPage();
    String type = request.getInput("type");

    page.setMainTemplate("refactorForm");
    page.setTitle("Refactor: " + resource);
    page.setPageTitle(new PageTitle("Refactor", PathParser.parse(resource), tags));
    page.put("refactoredRootPage", resource);
    page.put("request", request);
    page.put("type", type);
    page.put("viewLocation", request.getResource());
    if ("move".equals(type)) {
      String rootPath = context.getProperty("Refactor.StartPath");
      page.put("rootpath", StringUtils.isBlank(rootPath) ? "ROOT" : rootPath);
    }
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html(request));
    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
