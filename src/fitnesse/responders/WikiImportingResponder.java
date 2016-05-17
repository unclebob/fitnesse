// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.http.ChunkedResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;

public class WikiImportingResponder extends ChunkingResponder implements SecureResponder {
  private final WikiImporter importer;
  public PageData data;
  private WikiImportingTraverser wikiImportingTraverser;

  public WikiImportingResponder() {
    this(new WikiImporter());
  }

  public WikiImportingResponder(WikiImporter wikiImporter) {
    this.importer = wikiImporter;
  }

  public static void handleImportProperties(HtmlPage html, WikiPage page) {
    PageData pageData = page.getData();
    if (WikiImportProperty.isImportedSubWiki(pageData)) {
      html.setBodyClass("imported");
      WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
      html.put("sourceUrl", importProperty.getSourceUrl());
    }
  }

  @Override
  protected void doSending() throws Exception {
    data = page.getData();

    wikiImportingTraverser = initializeImporter();
    HtmlPage htmlPage = makeHtml();

    htmlPage.render(response.getWriter());

    response.close();
  }

  public WikiImportingTraverser initializeImporter() throws Exception {
    String remoteUrl = request.getInput("remoteUrl");
    setRemoteUserCredentialsOnImporter(importer);
    importer.setAutoUpdateSetting(request.hasInput("autoUpdate"));
    return new WikiImportingTraverser(importer, page, remoteUrl);
  }

  private void setRemoteUserCredentialsOnImporter(WikiImporter importer) {
    if (request.hasInput("remoteUsername"))
      importer.setRemoteUsername(request.getInput("remoteUsername"));
    if (request.hasInput("remotePassword"))
      importer.setRemotePassword(request.getInput("remotePassword"));
  }

  private HtmlPage makeHtml() throws Exception {
    HtmlPage html = context.pageFactory.newPage();
    String title = "Wiki Import";
    if (wikiImportingTraverser.isUpdate())
      title += " Update";
    String localPathName = PathParser.render(path);
    html.setTitle(title + ": " + localPathName);
    html.setPageTitle(new PageTitle(title, path));
    html.setMainTemplate("wikiImportingPage");
    html.put("isUpdate", wikiImportingTraverser.isUpdate());
    String pageName = PathParser.render(path);
    html.put("pageName", pageName);
    html.put("remoteUrl", importer.remoteUrl());
    html.put("importer", importer);
    html.put("PathParser", PathParser.class);
    html.put("importTraverser", wikiImportingTraverser);
    return html;
  }

  @Override
  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  public void setResponse(ChunkedResponse response) {
    this.response = response;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }

}
