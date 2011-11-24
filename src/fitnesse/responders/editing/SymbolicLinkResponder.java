// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.io.File;

import fitnesse.wikitext.parser.WikiWordBuilder;
import util.EnvironmentVariableTool;
import util.StringUtil;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wikitext.Utils;

public class SymbolicLinkResponder implements Responder {
  private Response response;
  private String resource;
  private PageCrawler crawler;
  private FitNesseContext context;
  private WikiPage page;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    resource = request.getResource();
    this.context = context;
    crawler = context.root.getPageCrawler();
    page = crawler.getPage(context.root, PathParser.parse(resource));
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    response = new SimpleResponse();
    if (request.hasInput("removal"))
      removeSymbolicLink(request, page);
    else if (request.hasInput("rename"))
      renameSymbolicLink(request, page);
    else
      addSymbolicLink(request, page);

    return response;
  }

  private void setRedirect(String resource) {
    response.redirect(resource + "?properties");
  }

  private void removeSymbolicLink(Request request, WikiPage page) throws Exception {
    String linkToRemove = (String) request.getInput("removal");

    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinks = getSymLinkProperty(properties);
    symLinks.remove(linkToRemove);
    if (symLinks.keySet().size() == 0)
      properties.remove(SymbolicPage.PROPERTY_NAME);
    page.commit(data);
    setRedirect(resource);
  }

  private void  renameSymbolicLink(Request request, WikiPage page) throws Exception {
    String linkToRename = (String) request.getInput("rename"),
      newName = (String) request.getInput("newname");

    if (page.hasChildPage(newName)) {
      response = new ErrorResponder(resource + " already has a child named " + newName + ".").makeResponse(context, null);
      response.setStatus(412);
    } else {
      PageData data = page.getData();
      WikiPageProperties properties = data.getProperties();
      WikiPageProperty symLinks = getSymLinkProperty(properties);
      String currentPath = symLinks.get(linkToRename);
      symLinks.remove(linkToRename);
      symLinks.set(newName, currentPath);
      page.commit(data);
      setRedirect(resource);
    }
  }

  private void addSymbolicLink(Request request, WikiPage page) throws Exception {
    String linkName = StringUtil.trimNonNullString((String) request.getInput("linkName"));
    String linkPath = StringUtil.trimNonNullString((String) request.getInput("linkPath"));
    if (isFilePath(linkPath) && !isValidDirectoryPath(linkPath)) {
      String message = "Cannot create link to the file system path, <b>" + linkPath + "</b>." +
        "<br/> The canonical file system path used was <b>" + createFileFromPath(linkPath).getCanonicalPath() + ".</b>" +
        "<br/>Either it doesn't exist or it's not a directory.";
      response = new ErrorResponder(message).makeResponse(context, null);
      response.setStatus(404);
    } else if (!isFilePath(linkPath) && isInternalPageThatDoesntExist(linkPath)) {
      response = new ErrorResponder("The page to which you are attempting to link, " + Utils.escapeHTML(linkPath) + ", doesn't exist.").makeResponse(context, null);
      response.setStatus(404);
    } else if (page.hasChildPage(linkName)) {
      response = new ErrorResponder(resource + " already has a child named " + linkName + ".").makeResponse(context, null);
      response.setStatus(412);
    } else {
      PageData data = page.getData();
      WikiPageProperties properties = data.getProperties();
      WikiPageProperty symLinks = getSymLinkProperty(properties);
      symLinks.set(linkName, linkPath);
      page.commit(data);
      setRedirect(resource);
    }
  }

  private boolean isValidDirectoryPath(String linkPath) throws Exception {
    File file = createFileFromPath(linkPath);

    if (file.exists())
      return file.isDirectory();
    else {
      File parentDir = file.getParentFile();
      return parentDir.exists() && parentDir.isDirectory();
    }
  }

  private File createFileFromPath(String linkPath) {
    String pathToFile = EnvironmentVariableTool.replace(linkPath.substring(7));
    return new File(pathToFile);
  }

  private boolean isFilePath(String linkPath) {
    return linkPath.startsWith("file://");
  }

  private boolean isInternalPageThatDoesntExist(String linkPath) throws Exception {
    String expandedPath = WikiWordBuilder.expandPrefix(page, linkPath);
    WikiPagePath path = PathParser.parse(expandedPath);
    WikiPage start = path.isRelativePath() ? page.getParent() : page; //TODO -AcD- a better way?
    return !crawler.pageExists(start, path);
  }

  private WikiPageProperty getSymLinkProperty(WikiPageProperties properties) {
    WikiPageProperty symLinks = properties.getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinks == null)
      symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
    return symLinks;
  }
}
