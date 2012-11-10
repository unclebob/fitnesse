// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;

public class DirectoryResponder implements SecureResponder {
  private String resource;
  private File requestedDirectory;
  private FitNesseContext context;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");

  public DirectoryResponder(String resource, File requestedFile) {
    this.resource = resource;
    requestedDirectory = requestedFile;
  }

  public Response makeResponse(FitNesseContext context, Request request) {
    this.context = context;

    SimpleResponse simpleResponse = new SimpleResponse();
    if (!resource.endsWith("/"))
      setRedirectForDirectory(simpleResponse);
    else
      simpleResponse.setContent(makeDirectoryListingPage());
    return simpleResponse;
  }

  private void setRedirectForDirectory(Response response) {
    if (!resource.startsWith("/"))
      resource = "/" + resource;
    response.redirect(resource + "/");
  }

  private String makeDirectoryListingPage() {
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Files: " + resource);
    //page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "/", "Files Section"));
    page.setPageTitle(new PageTitle("Files Section", resource, "/"));
    page.put("fileInfoList", makeFileInfo(FileUtil.getDirectoryListing(requestedDirectory)));
    page.setMainTemplate("directoryPage");
    return page.html();
  }


  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }

  private List<FileInfo> makeFileInfo(File[] files) {
    List<FileInfo> fileInfo = new ArrayList<FileInfo>();
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
      String name = file.getName();
      if (file.isDirectory()) {
        name += "/";
      }
      return name;
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
