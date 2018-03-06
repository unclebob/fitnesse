// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import fitnesse.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fitnesse.wiki.WikiPageLoadException;
import fitnesse.wiki.WikiPageProperty;

/**
 * These are the (old) page properties, used by the FileSystemPage.
 */
public class WikiPageProperties extends WikiPageProperty implements Serializable {
  private static final Logger LOG = Logger.getLogger(WikiPageProperties.class.getName());
  private static final long serialVersionUID = 1L;

  public WikiPageProperties() {
  }

  public WikiPageProperties(InputStream inputStream) {
    this();
    loadFromXmlStream(inputStream);
  }

  public WikiPageProperties(Element rootElement) {
    this();
    loadFromRootElement(rootElement);
  }

  public WikiPageProperties(WikiPageProperty that) {
    super(that);
  }

  public void loadFromXmlStream(InputStream inputStream) {
    Document document;
    try {
      document = XmlUtil.newDocument(inputStream);
    } catch (Exception e) {
      throw new WikiPageLoadException("Unable to parse XML from stream", e);
    }
    Element root = document.getDocumentElement();
    loadFromRootElement(root);
  }

  public void loadFromXml(String xml) {
    Document document;
    try {
      document = XmlUtil.newDocument(xml);
    } catch (Exception e) {
      throw new WikiPageLoadException("Unable to parse XML from string " + xml, e);
    }
    Element root = document.getDocumentElement();
    loadFromRootElement(root);
  }

  public void loadFromRootElement(Element root) {
    NodeList nodes = root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE)
        continue;
      String key = node.getNodeName();
      LoadElement(this, (Element) node, key);
    }
  }

  private void LoadElement(WikiPageProperty context, Element element, String key) {
    WikiPageProperty newProperty = new WikiPageProperty();
    context.set(key, newProperty);

    NodeList nodes = element.getChildNodes();
    if (element.hasAttribute("value"))
      newProperty.setValue(element.getAttribute("value"));
    else if (nodes.getLength() == 1)
      newProperty.setValue(nodes.item(0).getNodeValue());

    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      if (childNode instanceof Element)
        LoadElement(newProperty, (Element) childNode, childNode.getNodeName());
    }
  }

  public String toXml() throws IOException {
    Document document = XmlUtil.newDocument();
    document.appendChild(makeRootElement(document));
    return XmlUtil.xmlAsString(document);
  }

  public Element makeRootElement(Document document) {
    Element root = document.createElement("properties");
    List<String> keys = new ArrayList<>(keySet());
    Collections.sort(keys);

    for (String key : keys) {
      WikiPageProperty childProperty = getProperty(key);
      toXml(childProperty, key, document, root);
    }

    return root;
  }

  private void toXml(WikiPageProperty context, String key, Document document, Element parent) {
    Element element = document.createElement(key);

    String value = context.getValue();
    if (context.hasChildren()) {
      if (value != null)
        element.setAttribute("value", value);

      Set<String> childKeys = context.keySet();
      for (String childKeyAsString : childKeys) {
        WikiPageProperty child = context.getProperty(childKeyAsString);
        if (child == null) {
          LOG.warning("Property key \"" + childKeyAsString + "\" has null value for {" + context + "}");
        } else {
          toXml(child, childKeyAsString, document, element);
        }
      }
    } else if (value != null)
      element.appendChild(document.createTextNode(value));

    parent.appendChild(element);
  }

  @Override
  public String toString() {
    return super.toString("WikiPageProperties", 0);
  }
}
