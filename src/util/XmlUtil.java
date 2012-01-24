// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;

public class XmlUtil {
  private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  public static DocumentBuilder getDocumentBuilder() {
    try {
      return documentBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public static Document newDocument() {
    return getDocumentBuilder().newDocument();
  }

  public static Document newDocument(InputStream input) throws IOException, SAXException {
    try {
      return getDocumentBuilder().parse(input);
    } catch (SAXParseException e) {
      throw new SAXException(String.format("SAXParseException at line:%d, col:%d, %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }
  }

  public static Document newDocument(File input) throws IOException, SAXException {
    try {
      return getDocumentBuilder().parse(new InputSource(new InputStreamReader(new FileInputStream(input), "UTF-8")));
    } catch (SAXParseException e) {
      throw new SAXException(String.format("SAXParseException at line:%d, col:%d, %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }
  }

  public static Document newDocument(String input) throws IOException, SAXException {
    ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
    return newDocument(is);
  }

  public static Element getElementByTagName(Element element, String name) {
    NodeList nodes = element.getElementsByTagName(name);
    if (nodes.getLength() == 0)
      return null;
    else
      return (Element) nodes.item(0);
  }

  public static Element getLocalElementByTagName(Element context, String tagName) {
    NodeList childNodes = context.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element && tagName.equals(node.getNodeName()))
        return (Element) node;
    }
    return null;
  }

  public static String getTextValue(Element element, String name) {
    Element namedElement = getElementByTagName(element, name);
    return getElementText(namedElement);
  }

  public static String getLocalTextValue(Element element, String name) {
    Element namedElement = getLocalElementByTagName(element, name);
    return getElementText(namedElement);
  }

  public static String getElementText(Element namedElement) {
    if (namedElement == null)
      return null;
    NodeList nodes = namedElement.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node instanceof Text)
        return node.getNodeValue();
    }
    //throw new Exception("No child of " + namedElement.getNodeName() + " is a Text node");
    return null;
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

  public static String xmlAsString(Document doc) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    XmlWriter writer = new XmlWriter(outputStream);
    writer.write(doc);
    writer.flush();
    writer.close();
    String value = outputStream.toString();
    return value;
  }
}
