// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testrunner.WikiTestPageUtil;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.UrlPathVariableSource;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

public class VersionResponder implements SecureResponder {
  private String version;
  private String resource;
  private FitNesseContext context;

  public Response makeResponse(FitNesseContext context, Request request) {
    resource = request.getResource();
    version = (String) request.getInput("version");
    this.context = context;
    if (version == null)
      return new ErrorResponder("No version specified.").makeResponse(context, request);

    PageCrawler pageCrawler = context.root.getPageCrawler();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = pageCrawler.getPage(path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    String fullPathName = PathParser.render(page.getPageCrawler().getFullPath());
    HtmlPage html = makeHtml(fullPathName, page, context, request);

    SimpleResponse response = new SimpleResponse();
    response.setContent(html.html());

    return response;
  }

  private HtmlPage makeHtml(String name, WikiPage page, FitNesseContext context, Request request) {
    WikiPage pageVersion = page.getVersion(version);
    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("Version " + version + ": " + name);
    html.setPageTitle(new PageTitle("Version " + version, PathParser.parse(resource), pageVersion.getData().getAttribute(PageData.PropertySUITES)));
    // TODO: subclass actions for specific rollback behaviour.
    html.setNavTemplate("versionNav.vm");
    html.put("rollbackVersion", version);
    html.put("localPath", name);

    List<VersionInfo> versions = new ArrayList<VersionInfo>(page.getVersions());
    Collections.sort(versions);
    Collections.reverse(versions);
    String nextVersion = selectNextVersion(versions, version);
    html.put("nextVersion", nextVersion);
    String previousVersion = selectPreviousVersion(versions, version);
    html.put("previousVersion", previousVersion);

    html.setMainTemplate("wikiPage");
    html.put("content", new VersionRenderer(pageVersion, request));
    return html;
  }

  private String selectPreviousVersion(List<VersionInfo> versions, String current) {
    int i = 0;
    for (i=0; i<versions.size(); i++)
      if (versions.get(i).getName().equals(current))
        break;
    if(i<0 || i>versions.size()-2)
      return null;
    return versions.get(i+1).getName();
  }

  private String selectNextVersion(List<VersionInfo> versions, String current) {
    int i = 0;
    for (; i<versions.size(); i++)
      if (versions.get(i).getName().equals(current))
        break;
    if(i<1 || i>versions.size())
      return null;
    return versions.get(i-1).getName();
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public class VersionRenderer {
    private WikiPage page;
    private Request request;

    public VersionRenderer(WikiPage page) {
      this(page, null);
    }

    public VersionRenderer(WikiPage page, Request request) {
      super();
      this.page = page;
      this.request = request;
    }

    public String render() {
      if (WikiTestPage.isTestPage(page)) {
        WikiTestPage testPage = new WikiTestPage(page, new UrlPathVariableSource(
                context.variableSource, request == null?null : request.getMap()));
        return WikiTestPageUtil.makePageHtml(testPage,request);
      } else {
        return WikiPageUtil.makePageHtml(page,request);
      }
    }
  }
}