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
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RollbackResponder implements SecureResponder {
  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse();

    String resource = request.getResource();
    String version = (String) request.getInput("version");
    if (version == null)
      return new ErrorResponder("missing version").makeResponse(context, request);

    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.root.getPageCrawler().getPage(path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    PageData data = page.getDataVersion(version);

    page.commit(data);

    context.recentChanges.updateRecentChanges(data);
    response.redirect(context.contextRoot, resource);

    return response;
  }

  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }
}
