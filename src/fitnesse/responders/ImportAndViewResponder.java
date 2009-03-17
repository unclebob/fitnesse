// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class ImportAndViewResponder implements Responder, WikiImporterClient {
  private WikiPage page;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String resource = request.getResource();

    if ("".equals(resource))
      resource = "FrontPage";

    loadPage(resource, context);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    loadPageData();

    SimpleResponse response = new SimpleResponse();
    response.redirect(resource);

    return response;
  }

  protected void loadPage(String resource, FitNesseContext context) throws Exception {
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    page = crawler.getPage(context.root, path);
  }

  protected void loadPageData() throws Exception {
    PageData pageData = page.getData();

    WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());

    if (importProperty != null) {
      WikiImporter importer = new WikiImporter();
      importer.setWikiImporterClient(this);
      importer.parseUrl(importProperty.getSourceUrl());
      importer.importRemotePageContent(page);
    }
  }

  public void pageImported(WikiPage localPage) throws Exception {
  }

  public void pageImportError(WikiPage localPage, Exception e) throws Exception {
    e.printStackTrace();
  }
}
