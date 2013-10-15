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

public class DeleteFileResponder implements SecureResponder {
  public String resource;

  public Response makeResponse(FitNesseContext context, Request request) {
    Response response = new SimpleResponse();
    resource = request.getResource();
    String filename = (String) request.getInput("filename");
    String pathname = context.getRootPagePath() + "/" + resource + filename;
    File file = new File(pathname);

    if (file.isDirectory())
      FileVersionsControllerFactory.getVersionsController(context).deleteDirectory(file);
    else
      FileVersionsControllerFactory.getVersionsController(context).deleteFile(file);

    response.redirect("/" + resource);
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
