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
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.fs.FileVersion;

public class RenameFileResponder implements SecureResponder {

  @Override
  public Response makeResponse(FitNesseContext context, final Request request) throws Exception {
    Response response = new SimpleResponse();
    String resource = request.getResource();
    File rootPath = new File(context.getRootPagePath());
    final File pathName = new File(rootPath, resource);

    if (!FileResponder.isInFilesDirectory(rootPath, pathName)) {
      return new ErrorResponder("Invalid path: " + resource).makeResponse(context, request);
    }

    final String oldFileName = request.getInput("filename");
    final String newFileName = request.getInput("newName").trim();

    final File oldFile = new File(pathName, oldFileName);
    final File newFile = new File(pathName, newFileName);

    if (!FileResponder.isInFilesDirectory(rootPath, oldFile)) {
      return new ErrorResponder("Invalid path: " + oldFileName).makeResponse(context, request);
    }

    if (!FileResponder.isInFilesDirectory(rootPath, newFile)) {
      return new ErrorResponder("Invalid path: " + newFileName).makeResponse(context, request);
    }

    context.versionsController.rename(new FileVersion() {

      @Override
      public File getFile() {
        return newFile;
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
    }, oldFile);

    response.redirect(context.contextRoot, resource);
    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
