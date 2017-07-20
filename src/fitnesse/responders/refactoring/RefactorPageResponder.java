// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import java.util.ArrayList;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.TraversalListener;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.*;

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
      page.put("suiteMap", collectPageNames(wikiPage, context.getRootPage()));
    }
    SimpleResponse response = new SimpleResponse();
    response.setContent(page.html());
    return response;
  }

  List<String> collectPageNames(final WikiPage thisPage, WikiPage rootPage) {
    final List<String> pageNames = new ArrayList<>();
    if (thisPage != null) {
      final WikiPagePath thisPagePath = thisPage.getPageCrawler().getFullPath();
      rootPage.getPageCrawler().traverse(new TraversalListener<WikiPage>() {

        @Override
        public void process(WikiPage page) {
          WikiPagePath pagePath = page.getPageCrawler().getFullPath();
          pagePath.makeAbsolute();
          if (!thisPagePath.equals(pagePath) && !pagePath.isEmpty()) {
            pageNames.add(pagePath.toString());
          }
        }
      });
    }
    return pageNames;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
