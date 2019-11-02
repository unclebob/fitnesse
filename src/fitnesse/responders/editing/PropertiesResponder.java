// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlUtil;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static fitnesse.wiki.PageData.ACTION_ATTRIBUTES;
import static fitnesse.wiki.PageData.NAVIGATION_ATTRIBUTES;
import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTES;
import static fitnesse.wiki.PageData.SECURITY_ATTRIBUTES;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static fitnesse.wiki.WikiPageProperty.EDIT;
import static fitnesse.wiki.WikiPageProperty.FILES;
import static fitnesse.wiki.WikiPageProperty.HELP;
import static fitnesse.wiki.WikiPageProperty.LAST_MODIFIED;
import static fitnesse.wiki.WikiPageProperty.LAST_MODIFYING_USER;
import static fitnesse.wiki.WikiPageProperty.PROPERTIES;
import static fitnesse.wiki.WikiPageProperty.PRUNE;
import static fitnesse.wiki.WikiPageProperty.RECENT_CHANGES;
import static fitnesse.wiki.WikiPageProperty.REFACTOR;
import static fitnesse.wiki.WikiPageProperty.SEARCH;
import static fitnesse.wiki.WikiPageProperty.SECURE_READ;
import static fitnesse.wiki.WikiPageProperty.SECURE_TEST;
import static fitnesse.wiki.WikiPageProperty.SECURE_WRITE;
import static fitnesse.wiki.WikiPageProperty.SUITES;
import static fitnesse.wiki.WikiPageProperty.VERSIONS;
import static fitnesse.wiki.WikiPageProperty.WHERE_USED;

public class PropertiesResponder implements SecureResponder {
  private WikiPage page;
  public PageData pageData;
  private String resource;
  private WikiPagePath path;
  private SimpleResponse response;
  private HtmlPage html;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    response = new SimpleResponse();
    resource = request.getResource();
    path = PathParser.parse(resource);
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    page = crawler.getPage(path, new MockingPageCrawler());
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    pageData = page.getData();
    makeContent(context, request);
    response.setMaxAge(0);
    return response;
  }

  private void makeContent(FitNesseContext context, Request request) throws UnsupportedEncodingException {
    if ("json".equals(request.getInput("format"))) {
      JSONObject jsonObject = makeJson();
      response.setContent(jsonObject.toString(1));
    } else {
      String html = makeHtml(context, request);

      response.setContent(html);
    }
  }

  private JSONObject makeJson() {
    response.setContentType(Response.Format.JSON);
    JSONObject jsonObject = new JSONObject();
    String[] attributes = { TEST.toString(), SEARCH,
        EDIT, PROPERTIES, VERSIONS, REFACTOR,
        WHERE_USED, RECENT_CHANGES, SUITE.toString(),
        PRUNE, SECURE_READ, SECURE_WRITE,
        SECURE_TEST, FILES };
    for (String attribute : attributes)
      addJsonAttribute(jsonObject, attribute);
    if (pageData.hasAttribute(HELP)) {
      jsonObject.put(HELP, pageData.getAttribute(HELP));
    }
    if (pageData.hasAttribute(SUITES)) {
      JSONArray tags = new JSONArray();
      for(String tag : pageData.getAttribute(SUITES).split(",")) {
        if (StringUtils.isNotBlank(tag)) {
          tags.put(tag.trim());
        }
      }
      jsonObject.put(SUITES, tags);
    }
    return jsonObject;
  }

  private void addJsonAttribute(JSONObject jsonObject, String attribute) {
    jsonObject.put(attribute, pageData.hasAttribute(attribute));
  }

  private String makeHtml(FitNesseContext context, Request request) {
    html = context.pageFactory.newPage();
    html.setNavTemplate("viewNav");
    html.put("viewLocation", request.getResource());
    html.setTitle("Properties: " + resource);

    String tags = "";
    if(pageData != null)  {
      tags = pageData.getAttribute(SUITES);
    }

    html.setPageTitle(new PageTitle("Page Properties", path, tags));
    html.put("pageData", pageData);
    html.setMainTemplate("propertiesPage");
    makeLastModifiedTag();
    makeFormSections();

    return html.html(request);
  }

  private void makeLastModifiedTag() {
    String username = pageData.getAttribute(LAST_MODIFYING_USER);
    String dateString = pageData.getAttribute(LAST_MODIFIED);
  if (dateString == null) dateString ="";
  if (!dateString.isEmpty()){
    try {
      Date date = WikiPageProperty.getTimeFormat().parse(dateString);
      dateString = " on " + new SimpleDateFormat("MMM dd, yyyy").format(date) + " at " + new SimpleDateFormat("hh:mm:ss a").format(date);
    }
    catch (ParseException e) {
      dateString = " on " + dateString;
    }
  }

    if (username == null || "".equals(username))
      html.put("lastModified", "Last modified anonymously" + dateString);
    else
      html.put("lastModified", "Last modified by " + username + dateString) ;

  }

  private void makeFormSections() {
    makePropertiesForm();

    WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData
        .getProperties());
    if (importProperty != null)
      makeImportUpdateForm(importProperty);
    else
      makeImportForm();

    makeSymbolicLinkSection();
  }

  private void makePropertiesForm() {
    makePageTypeRadiosHtml(pageData);
    makeTestActionCheckboxesHtml();
    makeNavigationCheckboxesHtml();
    makeSecurityCheckboxesHtml();
  }

  public void makePageTypeRadiosHtml(PageData pageData) {
    html.put("pageTypes", PAGE_TYPE_ATTRIBUTES);
    html.put("selectedPageType", getCheckedAttribute(pageData, PAGE_TYPE_ATTRIBUTES));
  }

  private String getCheckedAttribute(PageData pageData, String[] attributes) {
    for (int i = attributes.length - 1; i > 0; i--) {
      if (pageData.hasAttribute(attributes[i]))
        return attributes[i];
    }
    return attributes[0];
  }

  private void makeImportForm() {
    html.put("makeImportForm", true);
    html.put("autoUpdate", true);
  }

  private void makeImportUpdateForm(WikiImportProperty importProps) {
    if (importProps.isRoot()) {
      html.put("makeImportRootForm", true);
    } else {
      html.put("makeImportSubpageForm", true);
    }
    if (importProps.isAutoUpdate())
      html.put("autoUpdate", true);
    html.put("sourceUrl", importProps.getSourceUrl());
  }

  private void makeSymbolicLinkSection() {
    WikiPageProperty symLinksProperty = pageData.getProperties().getProperty(
        SymbolicPage.PROPERTY_NAME);
    if (symLinksProperty == null)
      return;
    List<Symlink> symlinks = new ArrayList<>();
    Set<String> symbolicLinkNames = symLinksProperty.keySet();
    for (String name : symbolicLinkNames) {
      String link = symLinksProperty.get(name);

      String path = makePathForSymbolicLink(link);
      symlinks.add(new Symlink(name, HtmlUtil.escapeHTML(link), path));
    }
    html.put("symlinks", symlinks);
  }

  private String makePathForSymbolicLink(String linkPath) {
    WikiPagePath wikiPagePath = PathParser.parse(linkPath);

    if (wikiPagePath != null) {
      WikiPage parent = wikiPagePath.isRelativePath() ? page.getParent() : page;
      PageCrawler crawler = parent.getPageCrawler();
      WikiPage target = crawler.getPage(wikiPagePath);
      WikiPagePath fullPath;
      if (target != null) {
        fullPath = target.getFullPath();
        fullPath.makeAbsolute();
      } else
        fullPath = new WikiPagePath();
      return fullPath.toString();
    }
    return null;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public void makeTestActionCheckboxesHtml() {
    html.put("actionTypes", ACTION_ATTRIBUTES);
  }

  public void makeNavigationCheckboxesHtml() {
    html.put("navigationTypes", NAVIGATION_ATTRIBUTES);
  }

  public void makeSecurityCheckboxesHtml() {
    html.put("securityTypes", SECURITY_ATTRIBUTES);
  }


  public static class Symlink {

    private String name, link, path;

    Symlink(String name, String link, String path) {
      this.name = name;
      this.link = link;
      this.path = path;
    }

    public String getName() {
      return name;
    }

    public String getLink() {
      return link;
    }

    public String getPath() {
      return path;
    }
  }
}
