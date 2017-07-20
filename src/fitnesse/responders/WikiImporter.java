// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fitnesse.util.XmlUtil;
import fitnesse.components.TraversalListener;
import fitnesse.http.RequestBuilder;
import fitnesse.http.ResponseParser;
import fitnesse.wiki.*;
import fitnesse.wiki.fs.PageXmlizer;

public class WikiImporter implements XmlizerPageHandler, TraversalListener<WikiPage> {
  private String remoteUsername;
  private String remotePassword;

  private String remoteHostname;
  private int remotePort;
  private WikiPagePath localPath;
  private WikiPagePath remotePath = new WikiPagePath();
  private WikiPagePath relativePath = new WikiPagePath();
  protected WikiImporterClient importerClient;
  protected int importCount = 0;
  protected int unmodifiedCount = 0;
  private List<WikiPagePath> orphans = new LinkedList<>();
  private HashSet<WikiPagePath> pageCatalog;
  private boolean shouldDeleteOrphans = true;
  private WikiPagePath contextPath;
  private boolean autoUpdateSetting = true;
  private Exception caughtException;

  public WikiImporter() {
    importerClient = new NullWikiImporterClient();
    localPath = new WikiPagePath();
  }

  public WikiImporter(WikiImporterClient client) {
    importerClient = client;
    localPath = new WikiPagePath();
  }

  public void importWiki(WikiPage page) throws IOException {
    catalogLocalTree(page);

    Document remotePageTreeDocument;
    try {
      remotePageTreeDocument = getPageTree();
    } catch (AuthenticationRequiredException e) {
      throw e;
    } catch (Exception e) {
      throw new WikiImporterException("Unable to process page tree", e);
    }
    new PageXmlizer().deXmlizeSkippingRootLevel(remotePageTreeDocument, page, this);

    configureAutoUpdateSetting(page);

    filterOrphans(page);
    if (shouldDeleteOrphans)
      removeOrphans(page);
  }

  private void removeOrphans(WikiPage context) {
    for (WikiPagePath orphan : orphans) {
      WikiPagePath path = orphan;
      WikiPage wikiPage = context.getPageCrawler().getPage(path);
      if (wikiPage != null)
        wikiPage.remove();
    }
  }

  private void filterOrphans(WikiPage context) {
    for (WikiPagePath aPageCatalog : pageCatalog) {
      WikiPagePath wikiPagePath = aPageCatalog;
      WikiPage unrecognizedPage = context.getPageCrawler().getPage(wikiPagePath);
      PageData data = unrecognizedPage.getData();
      WikiImportProperty importProps = WikiImportProperty.createFrom(data.getProperties());

      if (importProps != null && !importProps.isRoot()) {
        orphans.add(wikiPagePath);
      }
    }
  }

  private void catalogLocalTree(WikiPage page) {
    contextPath = page.getPageCrawler().getFullPath();
    pageCatalog = new HashSet<>();
    page.getPageCrawler().traverse(this);
    WikiPagePath relativePathOfContext = contextPath.subtractFromFront(contextPath);
    pageCatalog.remove(relativePathOfContext);
  }

  @Override
  public void enterChildPage(WikiPage childPage, Date lastModified) throws IOException {
    if (pageCatalog != null) {
      pageCatalog.remove(relativePath(childPage));
    }
    remotePath.addNameToEnd(childPage.getName());
    relativePath.addNameToEnd(childPage.getName());
    localPath.addNameToEnd(childPage.getName());

    PageData data = childPage.getData();
    WikiPageProperty props = data.getProperties();
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

  private void configureAutoUpdateSetting(WikiImportProperty importProps, PageData data, WikiPage childPage) {
    if (importProps.isAutoUpdate() != autoUpdateSetting) {
      importProps.setAutoUpdate(autoUpdateSetting);
      importProps.addTo(data.getProperties());
      childPage.commit(data);
    }
  }

  public void configureAutoUpdateSetting(WikiPage page) {
    PageData data = page.getData();
    WikiPageProperty props = data.getProperties();
    WikiImportProperty importProps = WikiImportProperty.createFrom(props);
    if (importProps != null)
      configureAutoUpdateSetting(importProps, data, page);
  }

  private WikiPagePath relativePath(WikiPage childPage) {
    return childPage.getPageCrawler().getFullPath().subtractFromFront(contextPath);
  }

  protected void importRemotePageContent(WikiPage localPage) throws IOException {
    try {
      Document doc = getXmlDocument("data");
      PageData remoteData = new PageXmlizer().deXmlizeData(doc);

      WikiPageProperty remoteProps = remoteData.getProperties();
      remoteProps.remove(PageData.PropertyEDIT);

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
      caughtException = e;
      importerClient.pageImportError(localPage, e);
    }
    importCount++;
  }

  public String remoteUrl() {
    String remotePathName = PathParser.render(remotePath);
    return "http://" + remoteHostname + ":" + remotePort + "/" + remotePathName;
  }

  @Override
  public void exitPage() {
    remotePath.removeNameFromEnd();
    relativePath.removeNameFromEnd();
    localPath.removeNameFromEnd();
  }

  public Document getPageTree() throws IOException, SAXException {
    return getXmlDocument("pages");
  }

  private Document getXmlDocument(String documentType) throws IOException, SAXException {
    String remotePathName = PathParser.render(remotePath);
    RequestBuilder builder = new RequestBuilder("/" + remotePathName);
    builder.addInput("responder", "proxy");
    builder.addInput("type", documentType);
    builder.setHostAndPort(remoteHostname, remotePort);
    if (remoteUsername != null)
      builder.addCredentials(remoteUsername, remotePassword);

    ResponseParser parser = ResponseParser.performHttpRequest(remoteHostname, remotePort, builder);

    if (parser.getStatus() == 404)
      throw new IOException("The remote resource, " + remoteUrl() + ", was not found.");
    if (parser.getStatus() == 401)
      throw new AuthenticationRequiredException(remoteUrl());

    String body = parser.getBody();
    return XmlUtil.newDocument(body);
  }

  public Exception getCaughtException() {
    return caughtException;
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

  // used by template
  public int getUnmodifiedCount() {
    return unmodifiedCount;
  }

  // used by template
  public int getImportCount() {
    return importCount;
  }

  public void parseUrl(String urlString) throws MalformedURLException {
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

  @Override
  public void process(WikiPage page) {
    WikiPagePath relativePath = relativePath(page);
    pageCatalog.add(relativePath);
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

    @Override
    public void pageImported(WikiPage localPage) {
    }

    @Override
    public void pageImportError(WikiPage localPage, Exception e) {
    }
  }

  public static class AuthenticationRequiredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthenticationRequiredException(String message) {
      super(message);
    }
  }

  public static class WikiImporterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WikiImporterException(String message, Throwable t) {
      super(message, t);
    }
  }

}
