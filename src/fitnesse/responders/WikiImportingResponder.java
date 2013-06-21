// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.http.ChunkedResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;

public class WikiImportingResponder extends ChunkingResponder implements SecureResponder, WikiImporterClient, Traverser<Object> {
  private boolean isUpdate;
  private boolean isNonRoot;
  public PageData data;

  private WikiImporter importer = new WikiImporter();
  private TraversalListener<Object> traversalListener;

  public static void handleImportProperties(HtmlPage html, WikiPage page) {
    PageData pageData = page.getData();
    if (WikiImportProperty.isImported(pageData)) {
      html.setBodyClass("imported");
      WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
      html.put("sourceUrl", importProperty.getSourceUrl());
    }
  }

  public void setImporter(WikiImporter importer) {
    this.importer = importer;
  }

  protected void doSending() throws Exception {
    data = page.getData();

    initializeImporter();
    HtmlPage htmlPage = makeHtml();

    htmlPage.render(response.getWriter());

    response.closeAll();
  }

  @Override
  public void traverse(TraversalListener<Object> traversalListener) {
    this.traversalListener = traversalListener;
    try {
      if (isNonRoot) {
        importer.importRemotePageContent(page);
      }

      importer.importWiki(page);

      if (!isUpdate) {
        WikiImportProperty importProperty = new WikiImportProperty(importer.remoteUrl());
        importProperty.setRoot(true);
        importProperty.setAutoUpdate(importer.getAutoUpdateSetting());
        importProperty.addTo(data.getProperties());
        page.commit(data);
      }
    }
    catch (WikiImporter.WikiImporterException e) {
      traversalListener.process(new ImportError("ERROR", "The remote resource, " + importer.remoteUrl() + ", was not found."));
    }
    catch (WikiImporter.AuthenticationRequiredException e) {
      traversalListener.process(new ImportError("AUTH", e.getMessage()));
    }
    catch (Exception e) {
      traversalListener.process(new ImportError("ERROR", e.getMessage(), e));
    }

  }

  public void initializeImporter() throws Exception {
    String remoteWikiUrl = establishRemoteUrlAndUpdateStyle();
    importer.setWikiImporterClient(this);
    importer.setLocalPath(path);
    importer.parseUrl(remoteWikiUrl);
    setRemoteUserCredentialsOnImporter();
    importer.setAutoUpdateSetting(request.hasInput("autoUpdate"));
  }

  private void setRemoteUserCredentialsOnImporter() {
    if (request.hasInput("remoteUsername"))
      importer.setRemoteUsername((String) request.getInput("remoteUsername"));
    if (request.hasInput("remotePassword"))
      importer.setRemotePassword((String) request.getInput("remotePassword"));
  }

  private String establishRemoteUrlAndUpdateStyle() throws Exception {
    String remoteWikiUrl = (String) request.getInput("remoteUrl");

    WikiImportProperty importProperty = WikiImportProperty.createFrom(data.getProperties());
    if (importProperty != null) {
      remoteWikiUrl = importProperty.getSourceUrl();
      isUpdate = true;
      isNonRoot = !importProperty.isRoot();
    }
    return remoteWikiUrl;
  }

  private HtmlPage makeHtml() throws Exception {
    HtmlPage html = context.pageFactory.newPage();
    html = context.pageFactory.newPage();
    String title = "Wiki Import";
    if (isUpdate)
      title += " Update";
    String localPathName = PathParser.render(path);
    html.setTitle(title + ": " + localPathName);
    html.setPageTitle(new PageTitle(title, path));
    html.setMainTemplate("wikiImportingPage");
    html.put("isUpdate", isUpdate);
    String pageName = PathParser.render(path);
    html.put("pageName", pageName);
    String remoteWikiUrl = importer.remoteUrl();
    html.put("remoteUrl", remoteWikiUrl);
    html.put("importer", importer);
    html.put("PathParser", PathParser.class);
    html.put("importTraverser", this);
    return html;
  }

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  public void setResponse(ChunkedResponse response) {
    this.response = response;
  }

  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }

  // Callback from importer
  public void pageImported(WikiPage localPage) {
    traversalListener.process(localPage);
  }

  // Callback from importer
  public void pageImportError(WikiPage localPage, Exception e) {
    traversalListener.process(new ImportError("PAGEERROR", e.getMessage(), e));
  }

  public WikiImporter getImporter() {
    return importer;
  }

  public static class ImportError {
    private String message;
    private String type;
    private Exception exception;

    public ImportError(String type, String message) {
      this(type, message, null);
    }

    public ImportError(String type, String message, Exception exception) {
      super();
      this.type = type;
      this.message = message;
      this.exception = exception;
    }

    public String getType() {
      return type;
    }

    public String getMessage() {
      return message;
    }

    public Exception getException() {
      return exception;
    }
  }
}
