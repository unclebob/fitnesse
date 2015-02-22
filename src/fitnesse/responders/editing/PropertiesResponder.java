// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.*;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fitnesse.wiki.PageData.*;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;

public class PropertiesResponder implements SecureResponder {
  private WikiPage page;
  public PageData pageData;
  private String resource;
  private WikiPagePath path;
  private SimpleResponse response;
  private HtmlPage html;

  public Response makeResponse(FitNesseContext context, Request request) {
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

  private void makeContent(FitNesseContext context, Request request) {
    if ("json".equals(request.getInput("format"))) {
      JSONObject jsonObject = makeJson();
      try {
        response.setContent(jsonObject.toString(1));
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    } else {
      String html = makeHtml(context, request);
      
      response.setContent(html);
    }
  }

  private JSONObject makeJson() {
    response.setContentType(Response.Format.JSON);
    JSONObject jsonObject = new JSONObject();
    String attributes[] = new String[] { TEST.toString(), PropertySEARCH,
        PropertyEDIT, PropertyPROPERTIES, PropertyVERSIONS, PropertyREFACTOR,
        PropertyWHERE_USED, PropertyRECENT_CHANGES, SUITE.toString(),
        PropertyPRUNE, PropertySECURE_READ, PropertySECURE_WRITE,
        PropertySECURE_TEST, PropertyFILES };
    for (String attribute : attributes)
      addJsonAttribute(jsonObject, attribute);
    if (pageData.hasAttribute(PropertyHELP)) {
      jsonObject.put(PropertyHELP, pageData.getAttribute(PropertyHELP));
    }
    if (pageData.hasAttribute(PropertySUITES)) {
      JSONArray tags = new JSONArray();
      for(String tag : pageData.getAttribute(PropertySUITES).split(",")) {
        if (StringUtils.isNotBlank(tag)) {
          tags.put(tag.trim());
        }
      }
      jsonObject.put(PropertySUITES, tags);
    }
    return jsonObject;
  }

  private void addJsonAttribute(JSONObject jsonObject, String attribute) {
    try {
      jsonObject.put(attribute, pageData.hasAttribute(attribute));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private String makeHtml(FitNesseContext context, Request request) {
    html = context.pageFactory.newPage();
    html.setNavTemplate("viewNav");
    html.put("viewLocation", request.getResource());
    html.setTitle("Properties: " + resource);
    
    String tags = "";
    if(pageData != null)  {
      tags = pageData.getAttribute(PageData.PropertySUITES); 
    }
    
    html.setPageTitle(new PageTitle("Page Properties", path, tags));
    html.put("pageData", pageData);
    html.setMainTemplate("propertiesPage");
    makeLastModifiedTag();
    makeFormSections();

    return html.html();
  }

  private void makeLastModifiedTag() {
    String username = pageData.getAttribute(LAST_MODIFYING_USER);
    if (username == null || "".equals(username))
      html.put("lastModified", "Last modified anonymously");
    else
      html.put("lastModified", "Last modified by " + username);
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
    List<Symlink> symlinks = new ArrayList<Symlink>();
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
        fullPath = target.getPageCrawler().getFullPath();
        fullPath.makeAbsolute();
      } else
        fullPath = new WikiPagePath();
      return fullPath.toString();
    }
    return null;
  }

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
