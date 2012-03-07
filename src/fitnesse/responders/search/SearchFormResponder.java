// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import static fitnesse.wiki.PageData.*;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.VelocityFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.HtmlPageFactory;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageType;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.StringWriter;

public class SearchFormResponder implements Responder {
  public static final String[] SEARCH_ACTION_ATTRIBUTES = { PropertyEDIT, PropertyVERSIONS,
    PropertyPROPERTIES, PropertyREFACTOR, PropertyWHERE_USED, PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH };
  public static final String[] SPECIAL_ATTRIBUTES = { "obsolete", "SetUp", "TearDown" };

  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse();

    HtmlPage html = context.htmlPageFactory.newPage();
    html.setMainTemplate("searchForm.vm");
    html.setTitle("Search Form");
    html.setPageTitle(new PageTitle("Search Form"));
    html.put("pageTypeAttributes", PageType.values());
    html.put("actionAttributes", SEARCH_ACTION_ATTRIBUTES);
    html.put("securityAttributes", SECURITY_ATTRIBUTES);
    html.put("specialAttributes", SPECIAL_ATTRIBUTES);
    html.put("searchedRootPage", request.getResource());
    html.put("request", request);

    response.setContent(html.html());

    return response;
  }

}
