// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;

public class XmlUtil {
  private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  public static DocumentBuilder getDocumentBuilder() throws Exception {
    return documentBuilderFactory.newDocumentBuilder();
  }

  public static Document newDocument() throws Exception {
    return getDocumentBuilder().newDocument();
  }

  public static Document newDocument(InputStream input) throws Exception {
    try {
      return getDocumentBuilder().parse(input);
    } catch (SAXParseException e) {
      throw new Exception(String.format("SAXParseException at line:%d, col:%d, %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));  
    }
  }

  public static Document newDocument(File input) throws Exception {
    try {
      return getDocumentBuilder().parse(new InputSource(new InputStreamReader(new FileInputStream(input), "UTF-8")));
    } catch (SAXParseException e) {
      throw new Exception(String.format("SAXParseException at line:%d, col:%d, %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }
  }

  public static Document newDocument(String input) throws Exception {
    ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
    return newDocument(is);
  }

  public static Element getElementByTagName(Element element, String name) throws Exception {
    NodeList nodes = element.getElementsByTagName(name);
    if (nodes.getLength() == 0)
      return null;
    else
      return (Element) nodes.item(0);
  }

  public static Element getLocalElementByTagName(Element context, String tagName) throws Exception {
    NodeList childNodes = context.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element && tagName.equals(node.getNodeName()))
        return (Element) node;
    }
    return null;
  }

  public static String getTextValue(Element element, String name) throws Exception {
    Element namedElement = getElementByTagName(element, name);
    return getElementText(namedElement);
  }

  public static String getLocalTextValue(Element element, String name) throws Exception {
    Element namedElement = getLocalElementByTagName(element, name);
    return getElementText(namedElement);
  }

  public static String getElementText(Element namedElement) throws Exception {
    if (namedElement == null)
      return null;
    Node candidateTextNode = namedElement.getFirstChild();
    if (candidateTextNode instanceof Text)
      return candidateTextNode.getNodeValue();
    else
      throw new Exception("The first child of " + namedElement.getNodeName() + " is not a Text node");
  }

  public static void addTextNode(Document document, Element element, String tagName, String value) {
    if (value != null && !(value.equals(""))) {
      Element titleElement = document.createElement(tagName);
      Text titleText = document.createTextNode(value);
      titleElement.appendChild(titleText);
      element.appendChild(titleElement);
    }
  }

  public static void addCdataNode(Document document, Element element, String tagName, String value) {
    if (value != null && !(value.equals(""))) {
      Element titleElement = document.createElement(tagName);
      CDATASection cData = document.createCDATASection(value);
      titleElement.appendChild(cData);
      element.appendChild(titleElement);
    }
  }

  public static String xmlAsString(Document doc) throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    XmlWriter writer = new XmlWriter(outputStream);
    writer.write(doc);
    writer.flush();
    writer.close();
    String value = outputStream.toString();
    return value;
  }
}
