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
  private static final int RESOURCE_SIZE_LIMIT = 262144;
  private static FileNameMap fileNameMap = URLConnection.getFileNameMap();
  final public String resource;
  final public File requestedFile;
  public Date lastModifiedDate;
  public String lastModifiedDateString;

  public static Responder makeResponder(Request request, String rootPath) {
    String resource = request.getResource();

    try {
      resource = URLDecoder.decode(resource, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return new ErrorResponder(e);
    }

    File requestedFile = new File(rootPath + "/" + resource);
    
    if (requestedFile.isDirectory())
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
      return makeFileResponse(context, request);
    } else if (canLoadFromClasspath(resource)) {
      return makeClasspathResponse(context, request);
    } else {
      return new NotFoundResponder().makeResponse(context, request);
    }
  }
  

  private boolean canLoadFromClasspath(String resource2) {
    return resource.startsWith("files/fitnesse/");
  }

  private Response makeClasspathResponse(FitNesseContext context, Request request) throws IOException {
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
    
    return response;
  }

  private Response makeFileResponse(FitNesseContext context, Request request) throws FileNotFoundException {
    InputStreamResponse response = new InputStreamResponse();
    determineLastModifiedInfo();

    if (isNotModified(request))
      return createNotModifiedResponse();
    else {
      response.setBody(requestedFile);
      setContentType(requestedFile.getName(), response);
      response.setLastModifiedHeader(lastModifiedDateString);
    }
    return response;
  }

  private boolean isNotModified(Request request) {
    if (request.hasHeader("If-Modified-Since")) {
      String queryDateString = (String) request.getHeader("If-Modified-Since");
      try {
        Date queryDate = SimpleResponse.makeStandardHttpDateFormat().parse(queryDateString);
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
    response.setStatus(304);
    response.addHeader("Date", SimpleResponse.makeStandardHttpDateFormat().format(Clock.currentDate()));
    response.addHeader("Cache-Control", "private");
    response.setLastModifiedHeader(lastModifiedDateString);
    return response;
  }

  private void determineLastModifiedInfo() {
    lastModifiedDate = new Date(requestedFile.lastModified());
    lastModifiedDateString = SimpleResponse.makeStandardHttpDateFormat().format(lastModifiedDate);

    try  // remove milliseconds
    {
      lastModifiedDate = SimpleResponse.makeStandardHttpDateFormat().parse(lastModifiedDateString);
    }
    catch (java.text.ParseException jtpe) {
      jtpe.printStackTrace();
    }
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
      } else {
        contentType = "text/plain";
      }
    }
    return contentType;
  }
}
