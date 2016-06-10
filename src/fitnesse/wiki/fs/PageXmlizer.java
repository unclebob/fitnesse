// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import fitnesse.util.XmlUtil;
import fitnesse.wiki.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PageXmlizer {
  private final DateFormat dateFormat = WikiPageProperty.getTimeFormat();
  private LinkedList<XmlizePageCondition> pageConditions = new LinkedList<>();

  public Document xmlize(WikiPage page) {
    Document document = XmlUtil.newDocument();
    Element pageElement = createXmlFromPage(document, page);
    document.appendChild(pageElement);

    return document;
  }

  public void deXmlize(Document doc, WikiPage context, XmlizerPageHandler handler) throws IOException {
    Element pageElement = doc.getDocumentElement();
    addChildFromXml(pageElement, context, handler);
  }

  public void deXmlizeSkippingRootLevel(Document document, WikiPage context, XmlizerPageHandler handler) throws IOException {
    Element pageElement = document.getDocumentElement();
    addChildrenFromXml(pageElement, context, handler);
  }

  public Document xmlize(PageData data) {
    Document document = XmlUtil.newDocument();
    Element dataElement = document.createElement("data");
    XmlUtil.addCdataNode(dataElement, "content", data.getContent());

    Element propertiesElement = new WikiPageProperties(data.getProperties()).makeRootElement(document);
    dataElement.appendChild(propertiesElement);

    document.appendChild(dataElement);

    return document;
  }

  public PageData deXmlizeData(Document document) {
    Element dataElement = document.getDocumentElement();

    String content = XmlUtil.getLocalTextValue(dataElement, "content");

    Element propertiesElement = XmlUtil.getLocalElementByTagName(dataElement, "properties");
    WikiPageProperties properties = new WikiPageProperties(propertiesElement);

    return new PageData(content, properties);
  }

  private void addPageXmlToElement(Element context, WikiPage page) {
    if (pageMeetsConditions(page))
      context.appendChild(createXmlFromPage(context.getOwnerDocument(), page));
  }

  private boolean pageMeetsConditions(WikiPage page) {
    for (XmlizePageCondition xmlizePageCondition : pageConditions) {
      if (!xmlizePageCondition.canBeXmlized(page))
        return false;
    }
    return true;
  }

  private Element createXmlFromPage(Document document, WikiPage page) {
    Element pageElement = document.createElement("page");
    XmlUtil.addTextNode(pageElement, "name", page.getName());
    addLastModifiedTag(page, pageElement);

    addXmlFromChildren(page, pageElement);

    return pageElement;
  }

  private void addLastModifiedTag(WikiPage page, Element pageElement) {
    Date lastModificationTime = page.getData().getProperties().getLastModificationTime();
    String lastModifiedTimeString = dateFormat.format(lastModificationTime);
    XmlUtil.addTextNode(pageElement, "lastModified", lastModifiedTimeString);
  }

  private void addXmlFromChildren(WikiPage page, Element pageElement) {
    Element childrenElement = pageElement.getOwnerDocument().createElement("children");
    List<WikiPage> children = page.getChildren();
    Collections.sort(children);

    for (WikiPage child : children) {
      addPageXmlToElement(childrenElement, child);
    }
    pageElement.appendChild(childrenElement);
  }

  private void addChildFromXml(Element pageElement, WikiPage context, XmlizerPageHandler handler) throws IOException {
    String name = XmlUtil.getTextValue(pageElement, "name");
    String modifiedDateString = XmlUtil.getTextValue(pageElement, "lastModified");

    Date modifiedDate;
    try {
      modifiedDate = modifiedDateString == null ? new Date(0) : dateFormat.parse(modifiedDateString);
    } catch (ParseException e) {
      modifiedDate = new Date(0);
    }

    WikiPage childPage = context.getChildPage(name);
    if (childPage == null)
      childPage = context.addChildPage(name);
    handler.enterChildPage(childPage, modifiedDate);
    addChildrenFromXml(pageElement, childPage, handler);
    handler.exitPage();
  }

  private void addChildrenFromXml(Element pageElement, WikiPage contextPage, XmlizerPageHandler handler) throws IOException {
    Element childrenElement = XmlUtil.getLocalElementByTagName(pageElement, "children");
    NodeList childNodes = childrenElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if ("page".equals(node.getNodeName()))
        addChildFromXml((Element) node, contextPage, handler);
    }
  }

  public void addPageCondition(XmlizePageCondition xmlizePageCondition) {
    pageConditions.add(xmlizePageCondition);
  }
}
