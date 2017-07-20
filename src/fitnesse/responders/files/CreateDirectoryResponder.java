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

public class CreateDirectoryResponder implements SecureResponder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();

    String resource = request.getResource();
    String dirname = request.getInput("dirname");
    final File file = new File(new File(context.getRootPagePath(), resource), dirname);

    if (!FileResponder.isInFilesDirectory(new File(context.getRootPagePath()), file)) {
      return new ErrorResponder("Invalid path: " + file.getName()).makeResponse(context, request);
    }

    final String user = request.getAuthorizationUsername();
    if (!file.exists())
      context.versionsController.addDirectory(new FileVersion() {

        @Override
        public File getFile() {
          return file;
        }

        @Override
        public InputStream getContent() throws IOException {
          return null;
        }

        @Override
        public String getAuthor() {
          return user != null ? user : "";
        }

        @Override
        public Date getLastModificationTime() {
          return new Date();
        }
      });

    response.redirect(context.contextRoot, resource);
    return response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
