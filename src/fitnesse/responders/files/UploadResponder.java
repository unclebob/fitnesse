// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.http.UploadedFile;
import fitnesse.responders.ErrorResponder;
import fitnesse.wiki.fs.FileVersion;

public class UploadResponder implements SecureResponder {
  private static final Pattern filenamePattern = Pattern.compile("([^/\\\\]*[/\\\\])*([^/\\\\]*)");

  private String rootPath;

  public Response makeResponse(FitNesseContext context, Request request) throws IOException {
    rootPath = context.getRootPagePath();
    SimpleResponse response = new SimpleResponse();

    String resource = URLDecoder.decode(request.getResource(), "UTF-8");
    final UploadedFile uploadedFile = (UploadedFile) request.getInput("file");
    final String user = request.getAuthorizationUsername();

    if (uploadedFile.isUsable()) {

      final File file = makeFileToCreate(uploadedFile, resource);

      if (!FileResponder.isInFilesDirectory(new File(rootPath), file)) {
        return new ErrorResponder("Invalid path: " + uploadedFile.getName()).makeResponse(context, request);
      }

      context.versionsController.makeVersion(new FileVersion() {

        @Override
        public File getFile() {
          return file;
        }

        @Override
        public InputStream getContent() throws IOException {
          return new BufferedInputStream(new FileInputStream(uploadedFile.getFile()) {
            @Override
            public void close() throws IOException {
              super.close();
              uploadedFile.getFile().delete();
            }
          });
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
    }

    response.redirect(context.contextRoot, request.getResource());
    return response;
  }

  private File makeFileToCreate(UploadedFile uploadedFile, String resource) {
    String relativeFilename = makeRelativeFilename(uploadedFile.getName());
    String filename = relativeFilename;
    int prefix = 1;
    File file = new File(makeFullFilename(resource, filename));
    while (file.exists()) {
      filename = makeNewFilename(relativeFilename, prefix++);
      file = new File(makeFullFilename(resource, filename));
    }
    return file;
  }

  private String makeFullFilename(String resource, String filename) {
    return rootPath + "/" + resource + filename;
  }

  public static String makeRelativeFilename(String name) {
    Matcher match = filenamePattern.matcher(name);
    if (match.find())
      return match.group(2);
    else
      return name;
  }

  public static String makeNewFilename(String filename, int copyId) {
    String[] parts = filename.split("\\.");

    if (parts.length == 1)
      return filename + "_copy" + copyId;
    else {
      StringBuilder newName = new StringBuilder();
      for (int i = 0; i < parts.length - 1; i++) {
        if (i != 0)
          newName.append(".");
        newName.append(parts[i]);
      }
      newName.append("_copy").append(copyId).append(".").append(parts[parts.length - 1]);
      return newName.toString();
    }
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
