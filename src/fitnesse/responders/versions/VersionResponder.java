// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.testsystems.TestPage;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class VersionResponder implements SecureResponder {
  private String version;
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) {
    resource = request.getResource();
    version = (String) request.getInput("version");
    if (version == null)
      return new ErrorResponder("No version specified.").makeResponse(context, request);

    PageCrawler pageCrawler = context.root.getPageCrawler();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = pageCrawler.getPage(context.root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    String fullPathName = PathParser.render(pageCrawler.getFullPath(page));
    HtmlPage html = makeHtml(fullPathName, page, context);

    SimpleResponse response = new SimpleResponse();
    response.setContent(html.html());

    return response;
  }

  private HtmlPage makeHtml(String name, WikiPage page, FitNesseContext context) {
    PageData pageData = page.getDataVersion(version);
    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("Version " + version + ": " + name);
    html.setPageTitle(new PageTitle("Version " + version, PathParser.parse(resource), pageData.getAttribute(PageData.PropertySUITES)));
    // TODO: subclass actions for specific rollback behaviour.
    html.setNavTemplate("versionNav.vm");
    html.put("rollbackVersion", version);
    html.put("localPath", name);
    html.setMainTemplate("wikiPage");
    html.put("content", new VersionRenderer(pageData));
    return html;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
  
  public class VersionRenderer {
    private final PageData pageData;

    public VersionRenderer(PageData pageData) {
      super();
      this.pageData = pageData;
    }

    public String render() {
        PageData data;
        if (isTestPage(pageData)) {
          TestPage testPage = new TestPage(pageData);
          data = testPage.getDecoratedData();
        } else {
          data = pageData;
        }
        return HtmlUtil.makePageHtml(data);

    }

    private boolean isTestPage(PageData pageData) {
      return pageData.hasAttribute("Test");
    }

  }
}
