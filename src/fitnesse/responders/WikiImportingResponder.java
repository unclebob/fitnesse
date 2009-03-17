// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.ChunkedResponse;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class WikiImportingResponder extends ChunkingResponder implements SecureResponder, WikiImporterClient {
  private int alternation = 0;
  private boolean isUpdate;
  private boolean isNonRoot;
  public PageData data;

  private WikiImporter importer = new WikiImporter();

  public void setImporter(WikiImporter importer) {
    this.importer = importer;
  }

  protected void doSending() throws Exception {
    data = page.getData();
    HtmlPage html = makeHtml();
    response.add(html.preDivision);

    try {
      initializeImporter();
      addHeadContent();
      if (isNonRoot)
        importer.importRemotePageContent(page);

      importer.importWiki(page);

      addTailContent();

      if (!isUpdate) {
        WikiImportProperty importProperty = new WikiImportProperty(importer.remoteUrl());
        importProperty.setRoot(true);
        importProperty.setAutoUpdate(importer.getAutoUpdateSetting());
        importProperty.addTo(data.getProperties());
        page.commit(data);
      }
    }
    catch (MalformedURLException e) {
      writeErrorMessage(e);
    }
    catch (FileNotFoundException e) {
      writeErrorMessage("The remote resource, " + importer.remoteUrl() + ", was not found.");
    }
    catch (WikiImporter.AuthenticationRequiredException e) {
      writeAuthenticationForm(e.getMessage());
    }
    catch (Exception e) {
      writeErrorMessage(e);
    }

    response.add(html.postDivision);
    response.closeAll();
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

  private void writeErrorMessage(String message) throws Exception {
    HtmlTag alert = HtmlUtil.makeDivTag("centered");
    alert.add(new HtmlTag("h2", "Import Failure"));
    alert.add(message);
    response.add(alert.html());
  }

  private void writeErrorMessage(Exception e) throws Exception {
    writeErrorMessage(e.getMessage());
    HtmlTag pre = new HtmlTag("pre");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(output);
    e.printStackTrace(printStream);
    printStream.flush();
    pre.add(new String(output.toByteArray()));
    response.add(pre.html());
  }

  private void addHeadContent() throws Exception {
    TagGroup head = new TagGroup();
    if (isUpdate)
      head.add("Updating imported wiki.");
    else
      head.add("Importing wiki.");
    head.add(" This may take a few moments.");
    head.add(HtmlUtil.BR);
    head.add(HtmlUtil.BR);
    head.add("Destination wiki: ");
    String pageName = PathParser.render(path);
    head.add(HtmlUtil.makeLink(pageName, pageName));

    head.add(HtmlUtil.BR);
    head.add("Source wiki: ");
    String remoteWikiUrl = importer.remoteUrl();
    head.add(HtmlUtil.makeLink(remoteWikiUrl, remoteWikiUrl));

    head.add(HtmlUtil.BR);
    head.add(HtmlUtil.BR);
    head.add("Imported pages:");
    head.add(HtmlUtil.HR);
    response.add(head.html());
  }

  private void addTailContent() throws Exception {
    TagGroup tail = makeTailHtml(importer);
    response.add(tail.html());
  }

  public TagGroup makeTailHtml(WikiImporter importer) throws Exception {
    TagGroup tail = new TagGroup();
    tail.add("<a name=\"end\"><hr></a>");
    tail.add(HtmlUtil.makeBold("Import complete. "));

    addUnmodifiedCount(importer, tail);
    tail.add(HtmlUtil.BR);
    addImportedPageCount(importer, tail);
    addOrphanedPageSection(importer, tail);
    addAutoUpdateMessage(importer, tail);

    return tail;
  }

  private void addAutoUpdateMessage(WikiImporter importer, TagGroup tail) {
    tail.add(HtmlUtil.BR);
    String message = "Automatic Update turned " + (importer.getAutoUpdateSetting() ? "ON" : "OFF");
    tail.add(message);
  }

  private void addUnmodifiedCount(WikiImporter importer, TagGroup tail) {
    if (importer.getUnmodifiedCount() != 0) {
      tail.add(HtmlUtil.BR);
      if (importer.getUnmodifiedCount() == 1)
        tail.add("1 page was unmodified.");
      else
        tail.add(importer.getUnmodifiedCount() + " pages were unmodified.");
    }
  }

  private void addImportedPageCount(WikiImporter importer, TagGroup tail) {
    if (importer.getImportCount() == 1)
      tail.add("1 page was imported.");
    else
      tail.add(importer.getImportCount() + " pages were imported.");
  }

  private void addOrphanedPageSection(WikiImporter importer, TagGroup tail) {
    List<WikiPagePath> orphans = importer.getOrphans();
    if (orphans.size() > 0) {
      tail.add(HtmlUtil.BR);
      if (orphans.size() == 1)
        tail.add("1 orphaned page was found and has been removed.");
      else
        tail.add(orphans.size() + " orphaned pages were found and have been removed.");
      tail.add(" This may occur when a remote page is deleted, moved, or renamed.");
      tail.add(HtmlUtil.BR);
      tail.add(HtmlUtil.BR);
      tail.add("Orphans:");
      tail.add(HtmlUtil.HR);

      for (Iterator<WikiPagePath> iterator = orphans.iterator(); iterator.hasNext();) {
        WikiPagePath path = iterator.next();
        HtmlTag row = alternatingRow();
        row.add(PathParser.render(path));
        tail.add(row);
      }
      tail.add(HtmlUtil.HR);
    }
  }

  private HtmlPage makeHtml() throws Exception {
    HtmlPage html = context.htmlPageFactory.newPage();
    html = context.htmlPageFactory.newPage();
    String title = "Wiki Import";
    if (isUpdate)
      title += " Update";
    String localPathName = PathParser.render(path);
    html.title.use(title + ": " + localPathName);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(localPathName, title));
    html.main.add(HtmlPage.BreakPoint);
    html.divide();
    return html;
  }

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  private void addRowToResponse(String status) throws Exception {
    HtmlTag tag = alternatingRow();
    String relativePathName = PathParser.render(importer.getRelativePath());
    String localPathName = PathParser.render(importer.getLocalPath());
    tag.add(HtmlUtil.makeLink(localPathName, relativePathName));
    tag.add(" " + status);
    response.add(tag.html());
  }

  private HtmlTag alternatingRow() {
    return HtmlUtil.makeDivTag("alternating_row_" + alternate());
  }

  private int alternate() {
    alternation = alternation % 2 + 1;
    return alternation;
  }

  public void setResponse(ChunkedResponse response) {
    this.response = response;
  }

  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }

  private void writeAuthenticationForm(String resource) throws Exception {
    HtmlTag html = HtmlUtil.makeDivTag("centered");
    html.add(new HtmlTag("h3", "The wiki at " + resource + " requires authentication."));
    html.add(HtmlUtil.BR);

    HtmlTag form = new HtmlTag("form");
    form.addAttribute("action", request.getResource());
    form.addAttribute("method", "post");
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
    if (request.hasInput("remoteUrl"))
      form.add(HtmlUtil.makeInputTag("hidden", "remoteUrl", (String) request.getInput("remoteUrl")));

    form.add("remote username: ");
    form.add(HtmlUtil.makeInputTag("text", "remoteUsername"));
    form.add(HtmlUtil.BR);
    form.add("remote password: ");
    form.add(HtmlUtil.makeInputTag("password", "remotePassword"));
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("submit", "submit", "Authenticate and Continue Import"));

    html.add(form);
    response.add(html.html());
  }

  public void pageImported(WikiPage localPage) throws Exception {
    addRowToResponse("");
  }

  public void pageImportError(WikiPage localPage, Exception e) throws Exception {
    addRowToResponse(e.toString());
  }

  public WikiImporter getImporter() {
    return importer;
  }
}
