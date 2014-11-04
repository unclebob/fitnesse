// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;

import util.Clock;
import util.StreamReader;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;

public class FileResponder implements Responder {
  // 1000-trick: remove milliseconds.
  private final static Date LAST_MODIFIED_FOR_RESOURCES = new Date((System.currentTimeMillis() / 1000) * 1000 );

  private static final int RESOURCE_SIZE_LIMIT = 262144;
  private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();
  final public String resource;
  final public File requestedFile;
  public Date lastModifiedDate;

  public static Responder makeResponder(Request request, String rootPath) throws IOException {
    String resource = request.getResource();

    try {
      resource = URLDecoder.decode(resource, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return new ErrorResponder(e);
    }

    File requestedFile = new File(rootPath, resource);

    if (!isInFilesDirectory(new File(rootPath), requestedFile)) {
      return new ErrorResponder("Invalid path: " + resource);
    } else if (requestedFile.isDirectory())
      return new DirectoryResponder(resource, requestedFile);
    else
      return new FileResponder(resource, requestedFile);
  }

  public FileResponder(String resource, File requestedFile) {
    this.resource = resource;
    this.requestedFile = requestedFile;
  }

  public Response makeResponse(FitNesseContext context, Request request) throws IOException {
    if (requestedFile.exists()) {
      return makeFileResponse(request);
    } else if (canLoadFromClasspath()) {
      return makeClasspathResponse(context, request);
    } else {
      return new NotFoundResponder().makeResponse(context, request);
    }
  }
  

  private boolean canLoadFromClasspath() {
    return resource.startsWith("files/fitnesse/");
  }

  private Response makeClasspathResponse(FitNesseContext context, Request request) throws IOException {

    determineLastModifiedInfo(LAST_MODIFIED_FOR_RESOURCES);

    if (isNotModified(request))
      return createNotModifiedResponse();

    String classpathResource = "/fitnesse/resources/" + resource.substring("files/fitnesse/".length());
    
    InputStream input = getClass().getResourceAsStream(classpathResource);
    if (input == null) {
      return new NotFoundResponder().makeResponse(context, request);
    }
    StreamReader reader = new StreamReader(input);
    // Set a hard limit on the amount of data that can be read:
    byte[] content = reader.readBytes(RESOURCE_SIZE_LIMIT);
    SimpleResponse response = new SimpleResponse();
    response.setContent(content);
    setContentType(classpathResource, response);
    lastModifiedDate = LAST_MODIFIED_FOR_RESOURCES;
    response.setLastModifiedHeader(lastModifiedDate);

    return response;
  }

  private Response makeFileResponse(Request request) throws FileNotFoundException {
    InputStreamResponse response = new InputStreamResponse();
    determineLastModifiedInfo(new Date(requestedFile.lastModified()));

    if (isNotModified(request))
      return createNotModifiedResponse();
    else {
      response.setBody(requestedFile);
      setContentType(requestedFile.getName(), response);
      response.setLastModifiedHeader(lastModifiedDate);
    }
    return response;
  }

  private boolean isNotModified(Request request) {
    if (request.hasHeader("If-Modified-Since")) {
      String queryDateString = (String) request.getHeader("If-Modified-Since");
      try {
        Date queryDate = Response.makeStandardHttpDateFormat().parse(queryDateString);
        if (!queryDate.before(lastModifiedDate))
          return true;
      }
      catch (ParseException e) {
        //Some browsers use local date formats that we can't parse.
        //So just ignore this exception if we can't parse the date.
      }
    }
    return false;
  }

  private Response createNotModifiedResponse() {
    Response response = new SimpleResponse();
    response.notModified(lastModifiedDate, Clock.currentDate());
    return response;
  }

  private void determineLastModifiedInfo(Date lastModified) {
    // remove milliseconds
    lastModifiedDate = new Date((lastModified.getTime() / 1000) * 1000);
  }

  private void setContentType(String filename, Response response) {
    String contentType = getContentType(filename);
    response.setContentType(contentType);
  }

  public static String getContentType(String filename) {
    String contentType = fileNameMap.getContentTypeFor(filename);
    if (contentType == null) {
      if (filename.endsWith(".css")) {
        contentType = "text/css";
      } else if (filename.endsWith(".js")) {
        contentType = "text/javascript";
      } else if (filename.endsWith(".jar")) {
        contentType = "application/x-java-archive";
      } else if ((filename.endsWith(".jpg")) || (filename.endsWith(".jpeg"))) {
          contentType = "image/jpeg";
      } else if (filename.endsWith(".png")) {
          contentType = "image/png";
      } else if (filename.endsWith(".gif")) {
          contentType = "image/gif";
      } else {
        contentType = "text/plain";
      }
    }
    return contentType;
  }

  public static boolean isInFilesDirectory(File rootPath, File file) throws IOException {
    return isInSubDirectory(new File(rootPath, "files").getCanonicalFile(),
            file.getCanonicalFile());
  }

  private static boolean isInSubDirectory(File dir, File file) {
    return file != null && (file.equals(dir) || isInSubDirectory(dir, file.getParentFile()));
  }
}
