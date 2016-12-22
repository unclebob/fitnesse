// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;

public class DeleteFileResponder implements SecureResponder {
  public String resource;

  @Override
  public Response makeResponse(FitNesseContext context, final Request request) throws Exception {
    Response response = new SimpleResponse();
    resource = request.getResource();
    String filename = request.getInput("filename");

    final File pathName = new File(new File(context.getRootPagePath(), resource), filename);

    if (!FileResponder.isInFilesDirectory(new File(context.getRootPagePath()), pathName)) {
      return new ErrorResponder("Invalid path: " + pathName.getName()).makeResponse(context, request);
    }

    context.versionsController.delete(pathName);

    response.redirect(context.contextRoot, resource);
    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
