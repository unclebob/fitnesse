// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import static fitnesse.wiki.PageData.PropertyEDIT;
import static fitnesse.wiki.PageData.PropertyFILES;
import static fitnesse.wiki.PageData.PropertyPROPERTIES;
import static fitnesse.wiki.PageData.PropertyPRUNE;
import static fitnesse.wiki.PageData.PropertyRECENT_CHANGES;
import static fitnesse.wiki.PageData.PropertyREFACTOR;
import static fitnesse.wiki.PageData.PropertySEARCH;
import static fitnesse.wiki.PageData.PropertyVERSIONS;
import static fitnesse.wiki.PageData.PropertyWHERE_USED;
import static fitnesse.wiki.PageData.SECURITY_ATTRIBUTES;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageType;

public class SearchFormResponder implements Responder {
  static final String[] SEARCH_ACTION_ATTRIBUTES = { PropertyEDIT, PropertyVERSIONS,
    PropertyPROPERTIES, PropertyREFACTOR, PropertyWHERE_USED };
  static final String[] SEARCH_NAVIGATION_ATTRIBUTES = { PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH };
  static final String SEARCH_ATTRIBUTE_SKIP = PropertyPRUNE;
  static final String[] SPECIAL_ATTRIBUTES = { "SetUp", "TearDown" };

  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse();

    HtmlPage html = context.pageFactory.newPage();
    html.setMainTemplate("searchForm");
    html.setTitle("Search Form");
    html.setPageTitle(new PageTitle("Search Form"));
    html.put("viewLocation", request.getResource());
    html.setNavTemplate("viewNav");
    html.put("pageTypeAttributes", PageType.values());
    html.put("actionAttributes", SEARCH_ACTION_ATTRIBUTES);
    html.put("navigationAttributes", SEARCH_NAVIGATION_ATTRIBUTES);
    html.put("securityAttributes", SECURITY_ATTRIBUTES);
    html.put("specialAttributes", SPECIAL_ATTRIBUTES);
    html.put("request", request);

    response.setContent(html.html());

    return response;
  }

}
