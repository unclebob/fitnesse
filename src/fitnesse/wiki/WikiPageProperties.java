// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import fitnesse.util.XmlUtil;
import fitnesse.components.XmlWriter;

public class WikiPageProperties implements Serializable
{

	private Map properties;
	public static final String VIRTUAL_WIKI_ATTRIBUTE = "VirtualWiki";
	private Map symbolicLinks;

	public WikiPageProperties() throws Exception
	{
		properties = new HashMap();
		symbolicLinks = new HashMap();
	}

	public WikiPageProperties(Map map) throws Exception
	{
		this();
		for(Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			if(!"false".equals(value))
				this.properties.put(key, value);
		}
	}

	public WikiPageProperties(InputStream inputStream) throws Exception
	{
		this();
		loadFromXmlStream(inputStream);
	}

	public WikiPageProperties(Element rootElement) throws Exception
	{
		this();
		loadFromRootElement(rootElement);
	}

	public WikiPageProperties(WikiPageProperties that) throws Exception
	{
		properties = new HashMap(that.properties);
		symbolicLinks = new HashMap(that.symbolicLinks);
	}

	public void loadFromXmlStream(InputStream inputStream) throws Exception
	{
		Document document = XmlUtil.newDocument(inputStream);
		Element root = document.getDocumentElement();
		loadFromRootElement(root);
	}

	public void loadFromRootElement(Element root) throws Exception
	{
		NodeList nodes = root.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String key = node.getNodeName();
			if(key.equals("symbolicLink"))
				loadSymbolicLink(node);
			else
			{
				String value = node.hasChildNodes() ? node.getFirstChild().getNodeValue() : "true";
				properties.put(key, value);
			}
		}
	}

	public void save(OutputStream outputStream) throws Exception
	{
		Document document = XmlUtil.newDocument();
		document.appendChild(makeRootElement(document));

		XmlWriter writer = new XmlWriter(outputStream);
		writer.write(document);
		writer.flush();
		writer.close();
	}

	public Element makeRootElement(Document document)
	{
		Element root = document.createElement("properties");
		List keys = new ArrayList(properties.keySet());
		Collections.sort(keys);

		for(Iterator iterator = keys.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = (String) properties.get(key);
			Element element = document.createElement(key);
			if(!"true".equals(value))
				element.appendChild(document.createTextNode(value));
			root.appendChild(element);
		}

		addSymbolicLinkElements(document, root);
		return root;
	}

	public boolean has(String key)
	{
		return properties.containsKey(key);
	}

	public String get(String key) throws Exception
	{
		return (String) properties.get(key);
	}

	public void set(String key, String value)
	{
		properties.put(key, value);
	}

	public void set(String key)
	{
		set(key, "true");
	}

	public void remove(String key)
	{
		properties.remove(key);
	}

	public Set keySet()
	{
		return properties.keySet();
	}

	public String toString()
	{
		StringBuffer s = new StringBuffer("WikiPageProperties:\n");
		for(Iterator iterator = properties.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = (String) properties.get(key);
			s.append("\t").append(key).append(" = ").append(value).append("\n");
		}
		symbolicLinksToString(s);
		return s.toString();
	}

	public void addSymbolicLink(String linkName, WikiPagePath pagePath)
	{
		symbolicLinks.put(linkName, pagePath);
	}

	public boolean hasSymbolicLink(String linkName)
	{
		return symbolicLinks.containsKey(linkName);
	}

	public WikiPagePath getSymbolicLink(String linkName)
	{
		return (WikiPagePath) symbolicLinks.get(linkName);
	}

	public Set getSymbolicLinkNames()
	{
		return symbolicLinks.keySet();
	}

	public void removeSymbolicLink(String linkName)
	{
		symbolicLinks.remove(linkName);
	}

	private void addSymbolicLinkElements(Document document, Element root)
	{
		for(Iterator iterator1 = symbolicLinks.keySet().iterator(); iterator1.hasNext();)
		{
			String linkName = (String) iterator1.next();
			WikiPagePath path = (WikiPagePath) symbolicLinks.get(linkName);
			Element linkElement = document.createElement("symbolicLink");
			XmlUtil.addTextNode(document, linkElement, "name", linkName);
			XmlUtil.addTextNode(document, linkElement, "path", PathParser.render(path));
			root.appendChild(linkElement);
		}
	}

	private void loadSymbolicLink(Node node) throws Exception
	{
		Element linkElement = (Element) node;
		String name = XmlUtil.getLocalTextValue(linkElement, "name");
		WikiPagePath path = PathParser.parse(XmlUtil.getLocalTextValue(linkElement, "path"));
		addSymbolicLink(name, path);
	}

	private void symbolicLinksToString(StringBuffer s)
	{
		s.append("\tSymbolic Links:\n");
		for(Iterator iterator = symbolicLinks.keySet().iterator(); iterator.hasNext();)
		{
			String linkName = (String) iterator.next();
			WikiPagePath path = getSymbolicLink(linkName);
			s.append("\t\t").append(linkName).append(" -> ").append(path).append("\n");
		}
	}
}
