// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import fitnesse.components.XmlWriter;
import fitnesse.components.XmlWriter;

public class XmlUtil
{
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	public static DocumentBuilder getDocumentBuilder() throws Exception
	{
		return documentBuilderFactory.newDocumentBuilder();
	}

	public static Document newDocument() throws Exception
	{
		return getDocumentBuilder().newDocument();
	}

	public static Document newDocument(InputStream input) throws Exception
	{
		return getDocumentBuilder().parse(input);
	}

	public static Document newDocument(String input) throws Exception
	{
		ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
		return newDocument(is);
	}

	public static Element getElementByTagName(Element element, String name) throws Exception
	{
		NodeList nodes = element.getElementsByTagName(name);
		if(nodes.getLength() == 0)
			return null;
		else
			return (Element) nodes.item(0);
	}

	public static Element getLocalElementByTagName(Element context, String tagName) throws Exception
	{
		NodeList childNodes = context.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			if(node instanceof Element && tagName.equals(node.getNodeName()))
      	return (Element)node;
		}
		return null;
	}

	public static String getTextValue(Element element, String name) throws Exception
	{
		Element namedElement = getElementByTagName(element, name);
		return getElementText(namedElement, name);
	}

	public static String getLocalTextValue(Element element, String name) throws Exception
	{
		Element namedElement = getLocalElementByTagName(element, name);
		return getElementText(namedElement, name);
	}

	private static String getElementText(Element namedElement, String name) throws Exception
	{
		if(namedElement == null)
			return null;
		Node candidateTextNode = namedElement.getFirstChild();
		if(candidateTextNode instanceof Text)
			return candidateTextNode.getNodeValue();
		else
			throw new Exception("The first child of " + name + " is not a Text node");
	}

	public static void addTextNode(Document document, Element element, String tagName, String value)
	{
		if(value != null && !(value.equals("")))
		{
			Element titleElement = document.createElement(tagName);
			Text titleText = document.createTextNode(value);
			titleElement.appendChild(titleText);
			element.appendChild(titleElement);
		}
	}

	public static void addCdataNode(Document document, Element element, String tagName, String value)
	{
		if(value != null && !(value.equals("")))
		{
			Element titleElement = document.createElement(tagName);
			CDATASection cData = document.createCDATASection(value);
			titleElement.appendChild(cData);
			element.appendChild(titleElement);
		}
	}

	public static String xmlAsString(Document doc) throws Exception
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XmlWriter writer = new XmlWriter(outputStream);
		writer.write(doc);
		writer.flush();
		writer.close();
		String value = outputStream.toString();
		return value;
	}
}
