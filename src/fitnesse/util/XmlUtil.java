// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.util;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import util.FileUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

public class XmlUtil {
  private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  private static ThreadLocal<DocumentBuilder> documentBuilder = new ThreadLocal<>();

  private static DocumentBuilder getDocumentBuilder() {
    DocumentBuilder builder = documentBuilder.get();
    if (builder == null) {
      try {
        builder = documentBuilderFactory.newDocumentBuilder();
      } catch (ParserConfigurationException e) {
        throw new IllegalStateException(e);
      }
      documentBuilder.set(builder);
    }
    return builder;
  }

  public static Document newDocument() {
    return getDocumentBuilder().newDocument();
  }

  public static Document newDocument(InputStream input) throws IOException, SAXException {
    return newDocument(new InputSource(input));
  }

  private static Document newDocument(InputSource source) throws IOException, SAXException {
    try {
      return getDocumentBuilder().parse(source);
    } catch (SAXParseException e) {
      throw new SAXException(String.format("SAXParseException at line:%d, col:%d, %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }
  }

  public static Document newDocument(File input) throws IOException, SAXException {
    try {
      return getDocumentBuilder().parse(new InputSource(new InputStreamReader(new FileInputStream(input), FileUtil.CHARENCODING)));
    } catch (SAXParseException e) {
      throw new SAXException(String.format("SAXParseException at %s:%d,%d: %s", input.getCanonicalPath(), e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
    }
  }

  public static Document newDocument(String input) throws IOException, SAXException {
    return newDocument(new InputSource(new StringReader(input)));
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
    if (namedElement == null) {
      return null;
    }
    String text = namedElement.getTextContent();
    return (text.isEmpty()) ? null : text;
  }

  public static void addTextNode(Element element, String tagName, String value) {
    if (value != null && !(value.equals(""))) {
      Element titleElement = element.getOwnerDocument().createElement(tagName);
      titleElement.setTextContent(value);
      element.appendChild(titleElement);
    }
  }

  public static void addCdataNode(Element element, String tagName, String value) {
    if (value != null && !(value.equals(""))) {
      Document document = element.getOwnerDocument();
      Element titleElement = document.createElement(tagName);
      titleElement.appendChild(document.createCDATASection(value));
      element.appendChild(titleElement);
    }
  }

  public static String xmlAsString(Document doc) throws IOException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();

    StringWriter sw = new StringWriter();
    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, FileUtil.CHARENCODING);
      transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      DOMSource source = new DOMSource(doc);

      StreamResult result =  new StreamResult(sw);
      transformer.transform(source, result);
    } catch (TransformerException e) {
      throw new IllegalStateException("Unable to serialize XML", e);
    }

    return sw.toString();
  }
}
