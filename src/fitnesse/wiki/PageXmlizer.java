// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.XmlUtil;

public class PageXmlizer {
  private static SimpleDateFormat dateFormat = WikiPageProperty.getTimeFormat();
  private LinkedList<XmlizePageCondition> pageConditions = new LinkedList<XmlizePageCondition>();

  public Document xmlize(WikiPage page) throws Exception {
    Document document = XmlUtil.newDocument();
    Element pageElement = createXmlFromPage(document, page);
    document.appendChild(pageElement);

    return document;
  }

  public void deXmlize(Document doc, WikiPage context, XmlizerPageHandler handler) throws Exception {
    Element pageElement = doc.getDocumentElement();
    addChildFromXml(pageElement, context, handler);
  }

  public void deXmlizeSkippingRootLevel(Document document, WikiPage context, XmlizerPageHandler handler) throws Exception {
    Element pageElement = document.getDocumentElement();
    addChildrenFromXml(pageElement, context, handler);
  }

  public Document xmlize(PageData data) throws Exception {
    Document document = XmlUtil.newDocument();
    Element dataElement = document.createElement("data");
    XmlUtil.addCdataNode(document, dataElement, "content", data.getContent());

    Element propertiesElement = data.getProperties().makeRootElement(document);
    dataElement.appendChild(propertiesElement);

    document.appendChild(dataElement);

    return document;
  }

  public PageData deXmlizeData(Document document) throws Exception {
    PageData data = new PageData(new WikiPageDummy());
    Element dataElement = document.getDocumentElement();

    String content = XmlUtil.getLocalTextValue(dataElement, "content");
    data.setContent(content);

    Element propertiesElement = XmlUtil.getLocalElementByTagName(dataElement, "properties");
    WikiPageProperties properties = new WikiPageProperties(propertiesElement);
    data.setProperties(properties);

    return data;
  }

  private void addPageXmlToElement(Document document, Element context, WikiPage page) throws Exception {
    if (pageMeetsConditions(page))
      context.appendChild(createXmlFromPage(document, page));
  }

  private boolean pageMeetsConditions(WikiPage page) throws Exception {
    for (Iterator<XmlizePageCondition> iterator = pageConditions.iterator(); iterator.hasNext();) {
      XmlizePageCondition xmlizePageCondition = iterator.next();
      if (!xmlizePageCondition.canBeXmlized(page))
        return false;
    }
    return true;
  }

  private Element createXmlFromPage(Document document, WikiPage page) throws Exception {
    Element pageElement = document.createElement("page");
    XmlUtil.addTextNode(document, pageElement, "name", page.getName());
    addLastModifiedTag(page, document, pageElement);

    addXmlFromChildren(page, document, pageElement);

    return pageElement;
  }

  private void addLastModifiedTag(WikiPage page, Document document, Element pageElement) throws Exception {
    Date lastModificationTime = page.getData().getProperties().getLastModificationTime();
    String lastModifiedTimeString = dateFormat.format(lastModificationTime);
    XmlUtil.addTextNode(document, pageElement, "lastModified", lastModifiedTimeString);
  }

  private void addXmlFromChildren(WikiPage page, Document document, Element pageElement) throws Exception {
    Element childrenElement = document.createElement("children");
    List<WikiPage> children = page.getChildren();
    Collections.sort(children);

    for (Iterator<WikiPage> iterator = children.iterator(); iterator.hasNext();) {
      WikiPage child = iterator.next();
      addPageXmlToElement(document, childrenElement, child);
    }
    pageElement.appendChild(childrenElement);
  }

  private void addChildFromXml(Element pageElement, WikiPage context, XmlizerPageHandler handler) throws Exception {
    String name = XmlUtil.getTextValue(pageElement, "name");
    String modifiedDateString = XmlUtil.getTextValue(pageElement, "lastModified");

    Date modifiedDate = modifiedDateString == null ? new Date(0) : dateFormat.parse(modifiedDateString);

    WikiPage childPage = context.getChildPage(name);
    if (childPage == null)
      childPage = context.addChildPage(name);
    handler.enterChildPage(childPage, modifiedDate);
    addChildrenFromXml(pageElement, childPage, handler);
    handler.exitPage();
  }

  private void addChildrenFromXml(Element pageElement, WikiPage contextPage, XmlizerPageHandler handler) throws Exception {
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
