// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;
import fitnesse.util.*;
import org.w3c.dom.*;

public class PageXmlizer
{
	public Document xmlize(WikiPage page) throws Exception
	{
		Document document = XmlUtil.newDocument();
		Element pageElement = createXmlFromPage(document, page);
		document.appendChild(pageElement);

		return document;
	}

	public void deXmlize(Document doc, WikiPage context, XmlizerPageHandler handler) throws Exception
	{
		Element pageElement = doc.getDocumentElement();
		addChildFromXml(pageElement, context, handler);
	}

	public void deXmlizeSkippingRootLevel(Document document, WikiPage context, XmlizerPageHandler handler) throws Exception
	{
		Element pageElement = document.getDocumentElement();
		addChildrenFromXml(pageElement, context, handler);
	}

	public Document xmlize(PageData data) throws Exception
	{
		Document document = XmlUtil.newDocument();
		Element dataElement = document.createElement("data");
		XmlUtil.addCdataNode(document, dataElement, "content", data.getContent());

		Element propertiesElement = data.getProperties().makeRootElement(document);
		dataElement.appendChild(propertiesElement);

		document.appendChild(dataElement);

		return document;
	}

	public PageData deXmlizeData(Document document) throws Exception
	{
		PageData data = new PageData(new MockWikiPage());
		Element dataElement = document.getDocumentElement();

		String content = XmlUtil.getLocalTextValue(dataElement, "content");
		data.setContent(content);

		Element propertiesElement = XmlUtil.getLocalElementByTagName(dataElement, "properties");
		WikiPageProperties properties = new WikiPageProperties(propertiesElement);
		data.setProperties(properties);

		return data;
	}

	private Element createXmlFromPage(Document document, WikiPage page) throws Exception
	{
		Element pageElement = document.createElement("page");
		XmlUtil.addTextNode(document, pageElement, "name", page.getName());

		addXmlFromChildren(page, document, pageElement);

		return pageElement;
	}

	private void addXmlFromChildren(WikiPage page, Document document, Element pageElement) throws Exception
	{
		Element childrenElement = document.createElement("children");
		List children = page.getChildren();
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			WikiPage child = (WikiPage) iterator.next();
			Element childElement = createXmlFromPage(document, child);
			childrenElement.appendChild(childElement);
		}
		pageElement.appendChild(childrenElement);
	}

	private void addChildFromXml(Element pageElement, WikiPage context, XmlizerPageHandler handler) throws Exception
	{
		String name = XmlUtil.getTextValue(pageElement, "name");
		WikiPage newPage = context.addChildPage(name);
		handler.pageAdded(newPage);
		addChildrenFromXml(pageElement, newPage, handler);
		handler.exitPage();
	}

	private void addChildrenFromXml(Element pageElement, WikiPage contextPage, XmlizerPageHandler handler) throws Exception
	{
		Element childrenElement = XmlUtil.getLocalElementByTagName(pageElement, "children");
		NodeList childNodes = childrenElement.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			if("page".equals(node.getNodeName()))
				addChildFromXml((Element)node, contextPage, handler);
		}
	}
}
