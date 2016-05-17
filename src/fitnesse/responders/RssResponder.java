// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.net.UnknownHostException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.wiki.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import fitnesse.util.XmlUtil;

public class RssResponder implements SecureResponder {
  private RssFeed feed;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage contextPage = getContextPage(context, request.getResource());
    WikiPage recentChangesPage = context.getRootPage().getChildPage(RecentChanges.RECENT_CHANGES);

    feed = new RssFeed(getConfiguredRssLinkPrefixFrom(contextPage));

    buildItemReportIfRecentChangesExists(recentChangesPage, request.getResource());

    return feed.asResponse();
  }

  private WikiPage getContextPage(FitNesseContext context, String resource) throws Exception {
    PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
    WikiPagePath resourcePath = PathParser.parse(resource);
    return pageCrawler.getPage(resourcePath);
  }

  protected void buildItemReportIfRecentChangesExists(WikiPage recentChangesPage, String resource)
      throws Exception {
    if (recentChangesPage != null)
      buildItemReport(resource, new RecentChangesPage(recentChangesPage));
  }

  private void buildItemReport(String resource, RecentChangesPage recentChangesPage)
      throws Exception {
    for (RecentChangesPageEntry line : recentChangesPage.getLinesApplicableTo(resource)) {
      feed.addItem(line);
    }
  }

  private String getConfiguredRssLinkPrefixFrom(WikiPage contextPage) throws Exception {
    if (contextPage == null) {
      return null;
    }
    return contextPage.getVariable("RSS_PREFIX");
  }

  protected static boolean isNeitherNullNorBlank(String string) {
    return string != null && !string.isEmpty();
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  static class RssFeed {
    private Element channelElement;
    private final Document document;
    private final LinkPrefixBuilder linkPrefixBuilder;

    public RssFeed(String configuredLinkPrefix) throws Exception {
      document = buildDocumentWithRssHeader();
      linkPrefixBuilder = new LinkPrefixBuilder(configuredLinkPrefix);
    }

    public void addItem(RecentChangesPageEntry line) throws Exception {
      Map<String, String> itemProperties = line.getItemProperties();
      Element itemElement = document.createElement("item");
      makeNodes(itemElement, itemProperties);
      linkPrefixBuilder.buildLink(itemElement, itemProperties.get("path"));

      String description = makeDescription(itemProperties);
      XmlUtil.addTextNode(itemElement, "description", description);
      channelElement.appendChild(itemElement);
    }

    public SimpleResponse asResponse() throws Exception {
      String bytes = XmlUtil.xmlAsString(document);
      SimpleResponse response = new SimpleResponse();
      response.setContent(bytes);
      response.setContentType("text/xml");
      return response;
    }

    private static String makeDescription(Map<String, String> itemProperties) {
      String description;
      String authoredBy = "";
      if (isNeitherNullNorBlank(itemProperties.get("author")))
        authoredBy = itemProperties.get("author") + ":";
      description = authoredBy + itemProperties.get("pubDate");
      return description;
    }

    private static void makeNodes(Element itemElement, Map<String, String> itemProperties) {
      XmlUtil.addTextNode(itemElement, "title", itemProperties.get("path"));
      XmlUtil.addTextNode(itemElement, "author", itemProperties.get("author"));
      XmlUtil.addTextNode(itemElement, "pubDate", itemProperties.get("pubDate"));
    }

    private Document buildDocumentWithRssHeader() throws Exception {
      Document rssDocument = XmlUtil.newDocument();
      Element rssDocumentElement = rssDocument.createElement("rss");
      rssDocument.appendChild(rssDocumentElement);
      channelElement = rssDocument.createElement("channel");
      rssDocumentElement.setAttribute("version", "2.0");
      rssDocumentElement.appendChild(channelElement);
      XmlUtil.addTextNode(channelElement, "title", "FitNesse:");

      return rssDocument;
    }
  }

  static class RecentChangesPage {
    private WikiPage page;

    RecentChangesPage(WikiPage page) {
      this.page = page;
    }

    public List<RecentChangesPageEntry> getLinesApplicableTo(String resource) throws Exception {
      List<RecentChangesPageEntry> filteredLines = new ArrayList<>();
      for (RecentChangesPageEntry line : getLines()) {
        if (line.relatesTo(resource))
          filteredLines.add(line);
      }
      return filteredLines;
    }

    private List<RecentChangesPageEntry> getLines() throws Exception {
      List<RecentChangesPageEntry> lines = new ArrayList<>();
      for (String lineString : getPageContentLines()) {
        lines.add(new RecentChangesPageEntry(lineString));
      }
      return lines;
    }

    private String[] getPageContentLines() throws Exception {
      PageData data = page.getData();
      String content = data.getContent();
      return content.split("\n");
    }
  }

  static class RecentChangesPageEntry {
    private String line;

    RecentChangesPageEntry(String line) {
      this.line = line;
    }

    public Map<String, String> getItemProperties() {
      String[] fields = convertTableLineToStrings();
      Map<String, String> itemProperties = new HashMap<>();
      itemProperties.put("path", fields[1]);
      itemProperties.put("author", fields[2]);
      itemProperties.put("pubDate", convertDateFormat(fields[3]));
      return itemProperties;
    }

    protected boolean relatesTo(String resource) {
      String path = getItemProperties().get("path");
      boolean blank = isNeitherNullNorBlank(resource);
      return !blank || path.startsWith(resource);
    }

    private String[] convertTableLineToStrings() {
      return line.split("\\|");
    }

    static String convertDateFormat(String dateIn) {
      // format matched kk:mm:ss EEE, MMM dd, yyyy
      Pattern timePattern = Pattern.compile("\\d*:\\d*:\\d* \\w*, \\w* \\d*, \\d*");
      Matcher m = timePattern.matcher(dateIn);
      if (m.matches())
        return (new SimpleDateFormat(FitNesseContext.rfcCompliantDateFormat))
            .format((new SimpleDateFormat(FitNesseContext.recentChangesDateFormat)).parse(dateIn,
                new ParsePosition(0)));
      else
        return dateIn;
    }
  }

  static class LinkPrefixBuilder {
    private String preconfiguredPrefix;

    LinkPrefixBuilder(String preconfiguredPrefix) {
      this.preconfiguredPrefix = preconfiguredPrefix;
    }

    public void buildLink(Element itemElement, String pageName)
        throws Exception {
      String prefix = getRssLinkPrefix();
      String link = prefix + pageName;

      XmlUtil.addTextNode(itemElement, "link", link);
    }

    private static String hostnameRssLinkPrefix() throws UnknownHostException {
      String hostName = java.net.InetAddress.getLocalHost().getHostName();
      return "http://" + hostName + "/";
    }

    private String getRssLinkPrefix() throws Exception {
      return preconfiguredPrefix == null ? hostnameRssLinkPrefix() : preconfiguredPrefix;
    }
  }
}
