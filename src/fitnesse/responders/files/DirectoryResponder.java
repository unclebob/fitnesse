// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;

// This responder is called from FileResponder in case
class DirectoryResponder implements SecureResponder {
  private String resource;
  private File requestedDirectory;
  private FitNesseContext context;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");

  public DirectoryResponder(String resource, File requestedFile) {
    this.resource = resource;
    requestedDirectory = requestedFile;
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    this.context = context;

    if (!resource.endsWith("/")) {
      return setRedirectForDirectory(request.getQueryString());
    } else if ("json".equals(request.getInput("format"))) {
      return makeDirectoryListingJsonPage();
    } else {
      return makeDirectoryListingPage();
    }
  }

  private Response setRedirectForDirectory(String queryString) {
    SimpleResponse simpleResponse = new SimpleResponse();
    simpleResponse.redirect(context.contextRoot, resource + "/" + (queryString != null ? "?" + queryString : ""));
    return simpleResponse;
  }

  private Response makeDirectoryListingPage() throws UnsupportedEncodingException {
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Files: " + resource);
    //page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "/", "Files Section"));
    page.setPageTitle(new PageTitle("Files Section", resource, "/"));
    page.put("fileInfoList", makeFileInfo(FileUtil.getDirectoryListing(requestedDirectory)));
    page.setMainTemplate("directoryPage");
    SimpleResponse simpleResponse = new SimpleResponse();
    simpleResponse.setContent(page.html());
    return simpleResponse;
  }

  private Response makeDirectoryListingJsonPage() throws UnsupportedEncodingException {
    JSONArray listing = new JSONArray();
    for (FileInfo fileInfo : makeFileInfo(FileUtil.getDirectoryListing(requestedDirectory))) {
      JSONObject fiObject = new JSONObject();
      fiObject.put("name", fileInfo.getName());
      fiObject.put("size", fileInfo.getSize());
      fiObject.put("date", fileInfo.getDate());
      fiObject.put("directory", fileInfo.isDirectory());
      listing.put(fiObject);
    }

    SimpleResponse simpleResponse = new SimpleResponse();
    simpleResponse.setContentType(Response.Format.JSON);
    simpleResponse.setContent(listing.toString());
    return simpleResponse;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }

  private List<FileInfo> makeFileInfo(File[] files) {
    List<FileInfo> fileInfo = new ArrayList<>();
    for (File file : files) {
      fileInfo.add(new FileInfo(file));
    }
    return fileInfo;
  }


  public class FileInfo {
    private File file;

    public FileInfo(File file) {
      this.file = file;
    }

    public File getFile() {
      return file;
    }

    public boolean isDirectory() {
      return file.isDirectory();
    }

    public String getName() {
      return file.getName();
    }

    public String getSize() {
      if (file.isDirectory())
        return "";
      else
        return file.length() + " bytes";
    }

    public String getDate() {
      return dateFormat.format(new Date(file.lastModified()));
    }
  }

}
