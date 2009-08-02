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
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageType;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.StringWriter;

public class SearchFormResponder implements Responder {
  public static final String[] SEARCH_ACTION_ATTRIBUTES = { PropertyEDIT, PropertyVERSIONS,
    PropertyPROPERTIES, PropertyREFACTOR, PropertyWHERE_USED, PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH };
  public static final String[] SPECIAL_ATTRIBUTES = { "obsolete", "SetUp", "TearDown" };

  public Response makeResponse(FitNesseContext context, Request request)
  throws Exception {
    SimpleResponse response = new SimpleResponse();

    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = VelocityFactory.getVelocityEngine().getTemplate("searchForm.vm");

    velocityContext.put("pageTitle", new PageTitle("Search Form"));
    velocityContext.put("pageTypeAttributes", PageType.values());
    velocityContext.put("actionAttributes", SEARCH_ACTION_ATTRIBUTES);
    velocityContext.put("securityAttributes", SECURITY_ATTRIBUTES);
    velocityContext.put("specialAttributes", SPECIAL_ATTRIBUTES);
    velocityContext.put("searchedRootPage", request.getResource());
    velocityContext.put("request", request);

    template.merge(velocityContext, writer);
    response.setContent(writer.toString());

    return response;
  }

}
