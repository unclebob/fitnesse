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

public class RenameFileResponder implements SecureResponder {
  private String resource;
  String newFilename;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    Response response = new SimpleResponse();
    resource = request.getResource();
    String filename = (String) request.getInput("filename");
    newFilename = (String) request.getInput("newName");
    newFilename = newFilename.trim();

    String pathname = context.rootPagePath + "/" + resource;
    File file = new File(pathname + filename);
    file.renameTo(new File(pathname + newFilename));
    response.redirect("/" + resource);
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
