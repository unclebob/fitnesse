// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.Responder;
import fitnesse.wiki.*;
import fitnesse.util.XmlUtil;
import fitnesse.http.*;
import org.w3c.dom.*;

public class RssResponderTest extends ResponderTest
{
	protected Element channelElement;
	protected Element rssElement;
	protected Document rssDoc;

	// Return an instance of the Responder being tested.
	protected Responder responderInstance()
	{
		return new RssResponder();
	}

	public void testEmptyRssReport() throws Exception
	{
		buildRssChannel();
		assertEquals("rss", rssElement.getTagName());
		assertEquals("2.0", rssElement.getAttribute("version"));
		assertNotNull(channelElement);
		assertEquals("FitNesse:", XmlUtil.getTextValue(channelElement, "title"));
	}

	public void testOneNewPage() throws Exception
	{
		NodeList items = getReportedItems("|MyNewPage|me|now|");
		assertEquals(1, items.getLength());
		String title = "MyNewPage";
		String author = "me";
		String pubDate = "now";
		String description = "me:now";
		checkItem(items.item(0), title, author, pubDate, description, "http://localhost/MyNewPage");
	}

	public void testTwoNewPages() throws Exception
	{
		String recentChangeOne = "|MyNewPage|me|now|";
		String recentChangeTwo = "|SomeOtherPage||later|";
		String recentChangesContent = recentChangeOne + "\n" + recentChangeTwo + "\n";
		NodeList items = getReportedItems(recentChangesContent);
		assertEquals(2, items.getLength());
		checkItem(items.item(0), "MyNewPage", "me", "now", "me:now", "http://localhost/MyNewPage");
		checkItem(items.item(1), "SomeOtherPage", null, "later", "later", "http://localhost/SomeOtherPage");
	}

	public void testReportedPagesSelectedByResource() throws Exception
	{
		request.setResource("FrontPage");
		String page1 = "|SomePage|me|now|";
		String page2 = "|FrontPage|me|now|";
		String page3 = "|FrontPage.MyPage|me|now|";
		String page4 = "|SomePage.FrontPage|me|now";

		String recentChangesContent = page1 + "\n" + page2 + "\n" + page3 + "\n" + page4 + "\n";
		NodeList items = getReportedItems(recentChangesContent);
		assertEquals(2, items.getLength());
		checkItem(items.item(0), "FrontPage", "me", "now", "me:now", "http://localhost/FrontPage");
		checkItem(items.item(1), "FrontPage.MyPage", "me", "now", "me:now", "http://localhost/FrontPage.MyPage");
	}

	public void testLinkWithSetPrefix() throws Exception
	{
		PageData data = root.getData();
		data.setContent("!define RSS_PREFIX {http://host/}\n");
		root.commit(data);

		NodeList items = getReportedItems("|PageName|author|date|");
		assertEquals(1, items.getLength());
		checkItem(items.item(0), "PageName", "author", "date", "author:date", "http://host/PageName");
	}

	public void testLinkWitDefaultPrefix() throws Exception
	{
		NodeList items = getReportedItems("|PageName|author|date|");
		assertEquals(1, items.getLength());
		checkItem(items.item(0), "PageName", "author", "date", "author:date", "http://localhost/PageName");
	}

	private void buildRssChannel() throws Exception
	{
		SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
		rssDoc = XmlUtil.newDocument(response.getContent());
		rssElement = rssDoc.getDocumentElement();
		channelElement = XmlUtil.getElementByTagName(rssElement, "channel");
	}

	private void checkItem(Node node, String title, String author, String pubDate, String description, String link) throws Exception
	{
		Element itemElement = (Element) node;
		assertEquals(title, XmlUtil.getTextValue(itemElement, "title"));
		assertEquals(author, XmlUtil.getTextValue(itemElement, "author"));
		assertEquals(pubDate, XmlUtil.getTextValue(itemElement, "pubDate"));
		assertEquals(description, XmlUtil.getTextValue(itemElement, "description"));
		assertEquals(link, XmlUtil.getTextValue(itemElement,"link"));
	}

	private NodeList getReportedItems(String recentChangesContent) throws Exception
	{
		crawler.addPage(root, PathParser.parse("RecentChanges"), recentChangesContent);
		buildRssChannel();
		NodeList items = channelElement.getElementsByTagName("item");
		return items;
	}
}
