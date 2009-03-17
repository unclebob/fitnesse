// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;

import util.XmlUtil;
import fitnesse.components.FitNesseTraversalListener;
import fitnesse.http.RequestBuilder;
import fitnesse.http.ResponseParser;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageXmlizer;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.XmlizerPageHandler;

public class WikiImporter implements XmlizerPageHandler, FitNesseTraversalListener {
  public static String remoteUsername;
  public static String remotePassword;

  private String remoteHostname;
  private int remotePort;
  private WikiPagePath localPath;
  private WikiPagePath remotePath = new WikiPagePath();
  private WikiPagePath relativePath = new WikiPagePath();
  protected WikiImporterClient importerClient;
  protected int importCount = 0;
  protected int unmodifiedCount = 0;
  private List<WikiPagePath> orphans = new LinkedList<WikiPagePath>();
  private HashSet<WikiPagePath> pageCatalog;
  private PageCrawler crawler;
  private boolean shouldDeleteOrphans = true;
  private WikiPagePath contextPath;
  private boolean autoUpdateSetting;

  public WikiImporter() {
    this.importerClient = new NullWikiImporterClient();
    this.localPath = new WikiPagePath();
  }

  public WikiImporter(WikiImporterClient client) {
    this.importerClient = client;
    this.localPath = new WikiPagePath();
  }

  public void importWiki(WikiPage page) throws Exception {
    catalogLocalTree(page);

    Document remotePageTreeDocument = getPageTree();
    new PageXmlizer().deXmlizeSkippingRootLevel(remotePageTreeDocument, page, this);

    configureAutoUpdateSetting(page);

    filterOrphans(page);
    if (shouldDeleteOrphans)
      removeOrphans(page);
  }

  private void removeOrphans(WikiPage context) throws Exception {
    for (WikiPagePath orphan : orphans) {
      WikiPagePath path = orphan;
      WikiPage wikiPage = crawler.getPage(context, path);
      if (wikiPage != null)
        wikiPage.getParent().removeChildPage(wikiPage.getName());
    }
  }

  private void filterOrphans(WikiPage context) throws Exception {
    for (WikiPagePath aPageCatalog : pageCatalog) {
      WikiPagePath wikiPagePath = aPageCatalog;
      WikiPage unrecognizedPage = crawler.getPage(context, wikiPagePath);
      PageData data = unrecognizedPage.getData();
      WikiImportProperty importProps = WikiImportProperty.createFrom(data.getProperties());

      if (importProps != null && !importProps.isRoot()) {
        orphans.add(wikiPagePath);
      }
    }
  }

  private void catalogLocalTree(WikiPage page) throws Exception {
    crawler = page.getPageCrawler();
    contextPath = crawler.getFullPath(page);
    pageCatalog = new HashSet<WikiPagePath>();
    page.getPageCrawler().traverse(page, this);
    WikiPagePath relativePathOfContext = contextPath.subtractFromFront(contextPath);
    pageCatalog.remove(relativePathOfContext);
  }

  public void enterChildPage(WikiPage childPage, Date lastModified) throws Exception {
    if (pageCatalog != null) {
      pageCatalog.remove(relativePath(childPage));
    }
    remotePath.addNameToEnd(childPage.getName());
    relativePath.addNameToEnd(childPage.getName());
    localPath.addNameToEnd(childPage.getName());

    PageData data = childPage.getData();
    WikiPageProperties props = data.getProperties();
    WikiImportProperty importProps = WikiImportProperty.createFrom(props);
    if (importProps != null) {
      Date lastRemoteModification = importProps.getLastRemoteModificationTime();
      if (lastModified.after(lastRemoteModification))
        importRemotePageContent(childPage);
      else {
        unmodifiedCount++;
        configureAutoUpdateSetting(importProps, data, childPage);
      }
    } else
      importRemotePageContent(childPage);
  }

  private void configureAutoUpdateSetting(WikiImportProperty importProps, PageData data, WikiPage childPage) throws Exception {
    if (importProps.isAutoUpdate() != autoUpdateSetting) {
      importProps.setAutoUpdate(autoUpdateSetting);
      importProps.addTo(data.getProperties());
      childPage.commit(data);
    }
  }

  public void configureAutoUpdateSetting(WikiPage page) throws Exception {
    PageData data = page.getData();
    WikiPageProperties props = data.getProperties();
    WikiImportProperty importProps = WikiImportProperty.createFrom(props);
    if (importProps != null)
      configureAutoUpdateSetting(importProps, data, page);
  }

  private WikiPagePath relativePath(WikiPage childPage) throws Exception {
    return crawler.getFullPath(childPage).subtractFromFront(contextPath);
  }

  protected void importRemotePageContent(WikiPage localPage) throws Exception {
    try {
      Document doc = getXmlDocument("data");
      PageData remoteData = new PageXmlizer().deXmlizeData(doc);

      WikiPageProperties remoteProps = remoteData.getProperties();
      remoteProps.remove("Edit");

      WikiImportProperty importProperty = new WikiImportProperty(remoteUrl());
      Date lastModificationTime = remoteProps.getLastModificationTime();
      importProperty.setLastRemoteModificationTime(lastModificationTime);
      importProperty.setAutoUpdate(autoUpdateSetting);
      importProperty.addTo(remoteProps);

      localPage.commit(remoteData);

      importerClient.pageImported(localPage);
    }
    catch (AuthenticationRequiredException e) {
      throw e;
    }
    catch (Exception e) {
      importerClient.pageImportError(localPage, e);
    }
    importCount++;
  }

  public String remoteUrl() {
    String remotePathName = PathParser.render(remotePath);
    return "http://" + remoteHostname + ":" + remotePort + "/" + remotePathName;
  }

  public void exitPage() {
    remotePath.removeNameFromEnd();
    relativePath.removeNameFromEnd();
    localPath.removeNameFromEnd();
  }

  public Document getPageTree() throws Exception {
    return getXmlDocument("pages");
  }

  private Document getXmlDocument(String documentType) throws Exception {
    String remotePathName = PathParser.render(remotePath);
    RequestBuilder builder = new RequestBuilder("/" + remotePathName);
    builder.addInput("responder", "proxy");
    builder.addInput("type", documentType);
    builder.setHostAndPort(remoteHostname, remotePort);
    if (remoteUsername != null)
      builder.addCredentials(remoteUsername, remotePassword);

    ResponseParser parser = ResponseParser.performHttpRequest(remoteHostname, remotePort, builder);

    if (parser.getStatus() == 404)
      throw new Exception("The remote resource, " + remoteUrl() + ", was not found.");
    if (parser.getStatus() == 401)
      throw new AuthenticationRequiredException(remoteUrl());

    String body = parser.getBody();
    return XmlUtil.newDocument(body);
  }

  public void setRemoteUsername(String username) {
    remoteUsername = username;
  }

  public void setRemotePassword(String password) {
    remotePassword = password;
  }

  public WikiPagePath getRelativePath() {
    return relativePath;
  }

  public WikiPagePath getLocalPath() {
    return localPath;
  }

  public String getRemoteHostname() {
    return remoteHostname;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public WikiPagePath getRemotePath() {
    return remotePath;
  }

  public int getUnmodifiedCount() {
    return unmodifiedCount;
  }

  public int getImportCount() {
    return importCount;
  }

  public void parseUrl(String urlString) throws Exception {
    URL url;
    try {
      url = new URL(urlString);
    }
    catch (MalformedURLException e) {
      throw new MalformedURLException(urlString + " is not a valid URL.");
    }

    remoteHostname = url.getHost();
    remotePort = url.getPort();
    if (remotePort == -1)
      remotePort = 80;

    String path = url.getPath();
    while (path.startsWith("/"))
      path = path.substring(1);

    remotePath = PathParser.parse(path);

    if (remotePath == null) {
      throw new MalformedURLException("The URL's resource path, " + path + ", is not a valid WikiWord.");
    }
  }

  public void setWikiImporterClient(WikiImporterClient client) {
    importerClient = client;
  }

  public void setLocalPath(WikiPagePath path) {
    localPath = path;
  }

  public List<WikiPagePath> getOrphans() {
    return orphans;
  }

  public void processPage(WikiPage page) throws Exception {
    WikiPagePath relativePath = relativePath(page);
    pageCatalog.add(relativePath);
  }

  public String getSearchPattern() throws Exception {
    return null;
  }

  public void setDeleteOrphanOption(boolean shouldDeleteOrphans) {
    this.shouldDeleteOrphans = shouldDeleteOrphans;
  }

  public boolean getAutoUpdateSetting() {
    return autoUpdateSetting;
  }

  public void setAutoUpdateSetting(boolean autoUpdateSetting) {
    this.autoUpdateSetting = autoUpdateSetting;
  }

  private static class NullWikiImporterClient implements WikiImporterClient {

    public void pageImported(WikiPage localPage) {
    }

    public void pageImportError(WikiPage localPage, Exception e) {
    }
  }

  public static class AuthenticationRequiredException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuthenticationRequiredException(String message) {
      super(message);
    }
  }

}
