// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.io.File;
import java.io.IOException;

import fitnesse.html.HtmlUtil;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wikitext.parser.WikiWordBuilder;
import fitnesse.wiki.VariableTool;
import org.apache.commons.lang.StringUtils;
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

public class SymbolicLinkResponder implements Responder {
  private final FileSystem fileSystem;
  private Response response;
  private String resource;
  private FitNesseContext context;
  private WikiPage page;

  public SymbolicLinkResponder(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public SymbolicLinkResponder() {
    this(new DiskFileSystem());
  }

  public Response makeResponse(FitNesseContext context, Request request) throws IOException {
    resource = request.getResource();
    this.context = context;
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    page = crawler.getPage(PathParser.parse(resource));
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
    response.redirect(context.contextRoot, resource + "?properties#symbolics");
  }

  private void removeSymbolicLink(Request request, WikiPage page) {
    String linkToRemove = (String) request.getInput("removal");

    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinks = getSymLinkProperty(properties);
    symLinks.remove(linkToRemove);
    if (symLinks.keySet().isEmpty())
      properties.remove(SymbolicPage.PROPERTY_NAME);
    page.commit(data);
    setRedirect(resource);
  }

  private void  renameSymbolicLink(Request request, WikiPage page) {
    String linkToRename = (String) request.getInput("rename"),
    newName = (String) request.getInput("newname");

    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinks = getSymLinkProperty(properties);

    if (isValidWikiPageName(newName, symLinks)) {
      String currentPath = symLinks.get(linkToRename);
      symLinks.remove(linkToRename);
      symLinks.set(newName, currentPath);
      page.commit(data);
      setRedirect(resource);
    }
  }

  private void addSymbolicLink(Request request, WikiPage page) throws IOException {
    String linkName = StringUtils.trim((String) request.getInput("linkName"));
    String linkPath = StringUtils.trim((String) request.getInput("linkPath"));

    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinks = getSymLinkProperty(properties);
    if (isValidLinkPathName(linkPath) && isValidWikiPageName(linkName, symLinks)) {
      symLinks.set(linkName, linkPath);
      page.commit(data);
      setRedirect(resource);
    }
  }

  private boolean isValidWikiPageName(String linkName, WikiPageProperty symLinks) {
    if (page.hasChildPage(linkName) && !symLinks.has(linkName)) {
      response = new ErrorResponder(resource + " already has a child named " + linkName + ".").makeResponse(context, null);
      response.setStatus(412);
      return false;
    } else if (!PathParser.isSingleWikiWord(linkName)) {
      response = new ErrorResponder(linkName + " is not a valid WikiWord.").makeResponse(context, null);
      response.setStatus(412);
      return false;
    }
    return true;
  }

  private boolean isValidLinkPathName(String linkPath) throws IOException {
    if (isFilePath(linkPath) && !isValidDirectoryPath(linkPath)) {
      String message = "Cannot create link to the file system path '" + linkPath + "'." +
              " The canonical file system path used was ;" + createFileFromPath(linkPath).getCanonicalPath() + "'." +
              " Either it doesn't exist or it's not a directory.";
      response = new ErrorResponder(message).makeResponse(context, null);
      response.setStatus(404);
      return false;
    } else if (!isFilePath(linkPath) && isInternalPageThatDoesntExist(linkPath)) {
      response = new ErrorResponder("The page to which you are attempting to link, " + HtmlUtil.escapeHTML(linkPath) + ", doesn't exist.").makeResponse(context, null);
      response.setStatus(404);
      return false;
    }
    return true;
  }

  private boolean isValidDirectoryPath(String linkPath) {
    File file = createFileFromPath(linkPath);

    if (fileSystem.exists(file))
      return fileSystem.isDirectory(file);
    else {
      File parentDir = file.getParentFile();
      return parentDir != null && fileSystem.exists(parentDir) && fileSystem.isDirectory(parentDir);
    }
  }

  private File createFileFromPath(String linkPath) {
    // See FileSystemSubWikiPageFactory.createExternalSymbolicLink(), also.
    String fullPageURI = new VariableTool(context.variableSource).replace(linkPath);
    return WikiPageUtil.resolveFileUri(fullPageURI, new File(context.rootPath));
  }

  private boolean isFilePath(String linkPath) {
    return linkPath.startsWith("file:");
  }

  private boolean isInternalPageThatDoesntExist(String linkPath) {
    String expandedPath = WikiWordBuilder.expandPrefix(page, linkPath);
    WikiPagePath path = PathParser.parse(expandedPath);
    if (path == null) {
      return true;
    }
    WikiPage start = path.isRelativePath() ? page.getParent() : page; //TODO -AcD- a better way?
    return !start.getPageCrawler().pageExists(path);
  }

  private WikiPageProperty getSymLinkProperty(WikiPageProperties properties) {
    WikiPageProperty symLinks = properties.getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinks == null)
      symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
    return symLinks;
  }
}
