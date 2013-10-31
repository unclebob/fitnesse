// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.fs.FileVersion;

public class RenameFileResponder implements SecureResponder {
  String newFilename;

  public Response makeResponse(FitNesseContext context, final Request request) throws IOException {
    Response response = new SimpleResponse();
    String resource = request.getResource();
    final String pathname = context.getRootPagePath() + "/" + resource;
    final String filename = (String) request.getInput("filename");
    newFilename = (String) request.getInput("newName");
    newFilename = newFilename.trim();

    context.versionsController.rename(new FileVersion() {

      @Override
      public File getFile() {
        return new File(pathname + newFilename);
      }

      @Override
      public InputStream getContent() throws IOException {
        return null;
      }

      @Override
      public String getAuthor() {
        String user = request.getAuthorizationUsername();
        return user != null ? user : "";
      }

      @Override
      public Date getLastModificationTime() {
        return new Date();
      }
    }, new File(pathname + filename));

    response.redirect("/" + resource);
    return response;
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
