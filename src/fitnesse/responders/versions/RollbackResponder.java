// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RollbackResponder implements SecureResponder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();

    String resource = request.getResource();
    String version = request.getInput("version");
    if (version == null)
      return new ErrorResponder("Missing version.").makeResponse(context, request);

    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.getRootPage().getPageCrawler().getPage(path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    WikiPage rollbackPage = page.getVersion(version);

    page.commit(rollbackPage.getData());

    context.recentChanges.updateRecentChanges(rollbackPage);
    response.redirect(context.contextRoot, resource);

    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }
}
