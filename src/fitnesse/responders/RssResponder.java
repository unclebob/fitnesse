// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.io.ByteArrayOutputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.XmlUtil;
import util.XmlWriter;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RssResponder implements Responder {
  protected Element channelElement;
  private String resource;
  private WikiPage contextPage;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    Document rssDocument = buildRssHeader();
    XmlUtil.addTextNode(rssDocument, channelElement, "title", "FitNesse:");

    contextPage = getContextPage(request, context);
    WikiPage recentChangesPage = context.root.getChildPage("RecentChanges");
    buildItemReportIfRecentChangesExists(recentChangesPage, rssDocument, request.getResource());
    SimpleResponse response = responseFrom(rssDocument);
    return response;
  }

  private WikiPage getContextPage(Request request, FitNesseContext context)
    throws Exception {
    resource = request.getResource();
    PageCrawler pageCrawler = context.root.getPageCrawler();
    WikiPagePath resourcePath = PathParser.parse(resource);
    return pageCrawler.getPage(context.root, resourcePath);
  }

  protected void buildItemReportIfRecentChangesExists(WikiPage recentChangesPage, Document rssDocument, String resource) throws Exception {
    if (recentChangesPage != null)
      buildItemReport(recentChangesPage, resource, rssDocument);
  }

  private void buildItemReport(WikiPage recentChangesPage, String resource, Document rssDocument) throws Exception {
    String[] lines = convertPageToArrayOfLines(recentChangesPage);
    for (String line : lines)
      reportRecentChangeItem(line, resource, rssDocument);
  }

  private void reportRecentChangeItem(String line, String resource, Document rssDocument) throws Exception {
    String[] fields = convertTableLineToStrings(line);
    String path = fields[1];
    String author = fields[2];
    String pubDate = fields[3];
    pubDate = convertDateFormat(pubDate);

    if (shouldReportItem(resource, path))
      buildItem(rssDocument, path, author, pubDate);
  }

  private String[] convertTableLineToStrings(String line) {
    return line.split("\\|");
  }

  protected String[] convertPageToArrayOfLines(WikiPage page) throws Exception {
    PageData data = page.getData();
    String recentChanges = data.getContent();
    String[] lines = recentChanges.split("\n");
    return lines;
  }

  protected boolean shouldReportItem(String resource, String title) {
    boolean blank = isNeitherNullNorBlank(resource);
    return !blank || title.startsWith(resource);
  }

  private void buildItem(Document rssDocument, String title, String author, String pubDate) throws Exception {
    Element itemElement1 = rssDocument.createElement("item");
    makeNodes(rssDocument, itemElement1, title, author, pubDate);
    buildLink(rssDocument, itemElement1, title);

    String description = makeDescription(author, pubDate);
    XmlUtil.addTextNode(rssDocument, itemElement1, "description", description);
    Element itemElement = itemElement1;
    channelElement.appendChild(itemElement);
  }

  protected void makeNodes(Document rssDocument, Element itemElement1, String title, String author, String pubDate) {
    XmlUtil.addTextNode(rssDocument, itemElement1, "title", title);
    XmlUtil.addTextNode(rssDocument, itemElement1, "author", author);
    XmlUtil.addTextNode(rssDocument, itemElement1, "pubDate", pubDate);
  }

  protected void buildLink(Document rssDocument, Element itemElement1, String pageName) throws Exception {
    String hostName = java.net.InetAddress.getLocalHost().getHostName();
    String prefix = "http://" + hostName + "/";
    if (contextPage != null) {
      PageData data = contextPage.getData();
      String prefixVariable = data.getVariable("RSS_PREFIX");
      prefix = prefixVariable == null ? prefix : prefixVariable;
    }
    String link = prefix + pageName;

    XmlUtil.addTextNode(rssDocument, itemElement1, "link", link);
  }

  protected String makeDescription(String author, String pubDate) {
    String description;
    String authoredBy = "";
    if (isNeitherNullNorBlank(author))
      authoredBy = author + ":";
    description = authoredBy + pubDate;
    return description;
  }

  protected boolean isNeitherNullNorBlank(String string) {
    return string != null && string.length() > 0;
  }

  private SimpleResponse responseFrom(Document rssDocument) throws Exception {
    byte[] bytes = toByteArray(rssDocument);
    SimpleResponse response = new SimpleResponse();
    response.setContent(bytes);
    response.setContentType("text/xml");
    return response;
  }

  private byte[] toByteArray(Document rssDocument) throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    XmlWriter writer = new XmlWriter(os);
    writer.write(rssDocument);
    writer.close();
    byte[] bytes = os.toByteArray();
    return bytes;
  }

  private Document buildRssHeader() throws Exception {
    Document rssDocument = XmlUtil.newDocument();
    Element rssDocumentElement = rssDocument.createElement("rss");
    rssDocument.appendChild(rssDocumentElement);
    channelElement = rssDocument.createElement("channel");
    rssDocumentElement.setAttribute("version", "2.0");
    rssDocumentElement.appendChild(channelElement);
    return rssDocument;
  }

  protected String convertDateFormat(String dateIn) {
    // format matched kk:mm:ss EEE, MMM dd, yyyy
    Pattern timePattern = Pattern.compile("\\d*:\\d*:\\d* \\w*, \\w* \\d*, \\d*");
    Matcher m = timePattern.matcher(dateIn);
    if (m.matches())
      return (new SimpleDateFormat(FitNesseContext.rfcCompliantDateFormat)).format((new SimpleDateFormat(
        FitNesseContext.recentChangesDateFormat)).parse(dateIn, new ParsePosition(0)));
    else
      return dateIn;
  }
}
