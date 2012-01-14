// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.*;

import java.util.*;

public class VersionSelectionResponder implements SecureResponder {
  private WikiPage page;
  private List<VersionInfo> versions;
  private List<String> ageStrings;
  private PageData pageData;
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    page = context.root.getPageCrawler().getPage(context.root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    pageData = page.getData();
    versions = getVersionsList(pageData);

    HtmlPage html = context.htmlPageFactory.newPage();
    html.setTitle("Version Selection: " + resource);
    html.setPageTitle(new PageTitle("Version Selection", PathParser.parse(resource)));
    html.put("versions", versions);
    html.setMainTemplate("versionSelection.vm");

    response.setContent(html.html());

    return response;
  }

  public static List<VersionInfo> getVersionsList(PageData data) {
    List<VersionInfo> list = new ArrayList<VersionInfo>(data.getVersions());
    Collections.sort(list);
    Collections.reverse(list);
    return list;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
