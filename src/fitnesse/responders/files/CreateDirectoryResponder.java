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

public class CreateDirectoryResponder implements SecureResponder {
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();

    String resource = request.getResource();
    String dirname = (String) request.getInput("dirname");
    String pathname = context.rootPagePath + "/" + resource + dirname;
    File file = new File(pathname);
    if (!file.exists())
      file.mkdir();

    response.redirect("/" + resource);
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
