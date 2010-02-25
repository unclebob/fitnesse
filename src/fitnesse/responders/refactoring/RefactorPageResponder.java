// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;

public class RefactorPageResponder implements SecureResponder {

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String resource = request.getResource();

    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = VelocityFactory.getVelocityEngine().getTemplate("refactorForm.vm");

    velocityContext.put("pageTitle", new PageTitle("Refactor", PathParser.parse(resource)));
    velocityContext.put("refactoredRootPage", resource);
    velocityContext.put("request", request);

    template.merge(velocityContext, writer);

    SimpleResponse response = new SimpleResponse();
    response.setContent(writer.toString());
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
