// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.XmlUtil;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;

public class RssResponderTest extends ResponderTestCase {
  protected Element channelElement;
  protected Element rssElement;
  protected Document rssDoc;
  private String date;
  private String rfcDate;
  private String hostName;

  // Return an instance of the Responder being tested.
  protected Responder responderInstance() {
    return new RssResponder();
  }

  public void setUp() throws Exception {
    super.setUp();
    SimpleDateFormat dateFormat = new SimpleDateFormat(FitNesseContext.recentChangesDateFormat);
    date = dateFormat.format(new Date());
    SimpleDateFormat rfcDateFormat = new SimpleDateFormat(FitNesseContext.rfcCompliantDateFormat);
    rfcDate = rfcDateFormat.format(new Date());
    hostName = java.net.InetAddress.getLocalHost().getHostName();
    Locale.setDefault(Locale.US);
  }

  public void testEmptyRssReport() throws Exception {
    buildRssChannel();
    assertEquals("rss", rssElement.getTagName());
    assertEquals("2.0", rssElement.getAttribute("version"));
    assertNotNull(channelElement);
    assertEquals("FitNesse:", XmlUtil.getTextValue(channelElement, "title"));
  }

  public void testOneNewPage() throws Exception {
    NodeList items = getReportedItems("|MyNewPage|me|" + date + "|");
    assertEquals(1, items.getLength());
    String title = "MyNewPage";
    String author = "me";
    String pubDate = rfcDate;
    String description = "me:" + rfcDate;
    checkItem(items.item(0), title, author, pubDate, description, "http://" + hostName + "/MyNewPage");
  }

  public void testTwoNewPages() throws Exception {
    String recentChangeOne = "|MyNewPage|me|" + date + "|";
    String recentChangeTwo = "|SomeOtherPage||" + date + "|";
    String recentChangesContent = recentChangeOne + "\n" + recentChangeTwo + "\n";
    NodeList items = getReportedItems(recentChangesContent);
    assertEquals(2, items.getLength());
    checkItem(items.item(0), "MyNewPage", "me", rfcDate, "me:" + rfcDate, "http://" + hostName + "/MyNewPage");
    checkItem(items.item(1), "SomeOtherPage", null, rfcDate, rfcDate, "http://" + hostName + "/SomeOtherPage");
  }

  public void testReportedPagesSelectedByResource() throws Exception {
    request.setResource("FrontPage");
    String page1 = "|SomePage|me|" + date + "|";
    String page2 = "|FrontPage|me|" + date + "|";
    String page3 = "|FrontPage.MyPage|me|" + date + "|";
    String page4 = "|SomePage.FrontPage|me|" + date;

    String recentChangesContent = page1 + "\n" + page2 + "\n" + page3 + "\n" + page4 + "\n";
    NodeList items = getReportedItems(recentChangesContent);
    assertEquals(2, items.getLength());
    checkItem(items.item(0), "FrontPage", "me", rfcDate, "me:" + rfcDate, "http://" + hostName + "/FrontPage");
    checkItem(items.item(1), "FrontPage.MyPage", "me", rfcDate, "me:" + rfcDate, "http://" + hostName
      + "/FrontPage.MyPage");
  }

  public void testLinkWithSetPrefix() throws Exception {
    PageData data = root.getData();
    data.setContent("!define RSS_PREFIX {http://host/}\n");
    root.commit(data);

    NodeList items = getReportedItems("|PageName|author|" + date + "|");
    assertEquals(1, items.getLength());
    checkItem(items.item(0), "PageName", "author", rfcDate, "author:" + rfcDate, "http://host/PageName");
  }

  public void testLinkWitDefaultPrefix() throws Exception {
    NodeList items = getReportedItems("|PageName|author|" + date + "|");
    assertEquals(1, items.getLength());
    checkItem(items.item(0), "PageName", "author", rfcDate, "author:" + rfcDate, "http://" + hostName + "/PageName");
  }

  public void testConvertDateFormat() throws Exception {
    SimpleDateFormat oldFormat = new SimpleDateFormat(FitNesseContext.recentChangesDateFormat);
    SimpleDateFormat newFormat = new SimpleDateFormat(FitNesseContext.rfcCompliantDateFormat);
    String inDate = oldFormat.format(new Date());
    String outDate = newFormat.format(new Date());
    RssResponder responder = new RssResponder();
    String convertedDate = responder.convertDateFormat(inDate);
    assertEquals(convertedDate, outDate);
  }

  public void testBadDateFormat() throws Exception {
    SimpleDateFormat oldFormat = new SimpleDateFormat("h:mm:ss a EEE MMM dd, yyyy");
    String inDate = oldFormat.format(new Date());
    RssResponder responder = new RssResponder();
    String convertedDate = responder.convertDateFormat(inDate);
    assertEquals(convertedDate, inDate);
  }

  private void buildRssChannel() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    rssDoc = XmlUtil.newDocument(response.getContent());
    rssElement = rssDoc.getDocumentElement();
    channelElement = XmlUtil.getElementByTagName(rssElement, "channel");
  }

  private void checkItem(Node node, String title, String author, String pubDate, String description, String link)
    throws Exception {
    Element itemElement = (Element) node;
    assertEquals(title, XmlUtil.getTextValue(itemElement, "title"));
    assertEquals(author, XmlUtil.getTextValue(itemElement, "author"));
    assertEquals(pubDate, XmlUtil.getTextValue(itemElement, "pubDate"));
    assertEquals(description, XmlUtil.getTextValue(itemElement, "description"));
    assertEquals(link, XmlUtil.getTextValue(itemElement, "link"));
  }

  private NodeList getReportedItems(String recentChangesContent) throws Exception {
    crawler.addPage(root, PathParser.parse("RecentChanges"), recentChangesContent);
    buildRssChannel();
    NodeList items = channelElement.getElementsByTagName("item");
    return items;
  }
}
