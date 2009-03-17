// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import junit.framework.TestCase;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtilTest extends TestCase {
  public void testCreateXMLDocumentFromString() throws Exception {
    Document doc = XmlUtil.newDocument("<test>test</test>");
    NodeList elements = doc.getElementsByTagName("test");
    assertEquals(1, elements.getLength());
  }

  public void testGetLocalElementByTagName() throws Exception {
    Document doc = XmlUtil.newDocument("<level1>" +
      "  <target1/>" +
      "  <level2>" +
      "    <target2/>" +
      "  </level2>" +
      "</level1>");
    Element level1 = doc.getDocumentElement();
    Element level2 = XmlUtil.getElementByTagName(level1, "level2");

    Element target1 = XmlUtil.getLocalElementByTagName(level1, "target1");
    assertNotNull(target1);

    Element target2 = XmlUtil.getLocalElementByTagName(level1, "target2");
    assertNull(target2);

    target2 = XmlUtil.getLocalElementByTagName(level2, "target2");
    assertNotNull(target2);
  }

  public void testAddCdataElement() throws Exception {
    Document doc = XmlUtil.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);

    XmlUtil.addCdataNode(doc, root, "mydata", "<>&#;");

    Element myDataElement = XmlUtil.getElementByTagName(root, "mydata");
    assertNotNull(myDataElement);
    Node childNode = myDataElement.getChildNodes().item(0);
    assertTrue(childNode instanceof CDATASection);
    CDATASection cData = (CDATASection) childNode;
    assertEquals("<>&#;", cData.getNodeValue());
  }
}
