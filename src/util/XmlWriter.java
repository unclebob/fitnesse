// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XmlWriter {
  private static String endl = System.getProperty("line.separator");

  private Writer writer;
  private boolean isNewLine;

  public XmlWriter(OutputStream os) throws Exception {
    writer = new OutputStreamWriter(os, "UTF-8");
  }

  public void write(Document doc) throws Exception {
    write("<?xml version=\"1.0\"?>");
    write(endl);
    write(doc.getDocumentElement(), 0);
  }

  public void write(NodeList nodes) throws Exception {
    write(nodes, 0);
  }

  public void write(Element element, int tabs) throws Exception {
    if (!isNewLine)
      write(endl);
    if (!element.hasChildNodes()) {
      writeTabs(tabs);
      write("<" + element.getTagName() + writeAttributes(element) + "/>");
    } else {
      writeTabs(tabs);
      write("<" + element.getTagName() + writeAttributes(element) + ">");
      write(element.getChildNodes(), tabs + 1);
      if (isNewLine)
        writeTabs(tabs);
      write("</" + element.getTagName() + ">");
    }
    write(endl);
  }

  private String writeAttributes(Element element) {
    StringBuffer attributeString = new StringBuffer();
    NamedNodeMap attributeMap = element.getAttributes();
    int length = attributeMap.getLength();
    for (int i = 0; i < length; i++) {
      Attr attributeNode = (Attr) attributeMap.item(i);
      String name = attributeNode.getName();
      String value = attributeNode.getValue();
      attributeString.append(" ").append(name).append("=\"").append(value).append("\"");
    }
    return attributeString.toString();
  }

  private void write(NodeList nodes, int tabs) throws Exception {
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      write(node, tabs);
    }
  }

  private void writeText(Text text) throws Exception {
    String nodeValue = text.getNodeValue();
    write(nodeValue.trim());
  }

  private void writeCdata(CDATASection cData) throws Exception {
    String cDataText = "<![CDATA[" + cData.getNodeValue() + "]]>";
    write(cDataText);
  }

  private void write(Node node, int tabs) throws Exception {
    if (node instanceof Element)
      write((Element) node, tabs);
    else if (node instanceof CDATASection)
      writeCdata((CDATASection) node);
    else if (node instanceof Text)
      writeText((Text) node);
    else
      throw new Exception("XmlWriter: unsupported node type: " + node.getClass());
  }

  private void writeTabs(int tabs) throws Exception {
    for (int i = 0; i < tabs; i++)
      write("\t");
  }

  private void write(String value) throws Exception {
    if (value == null || "".equals(value)) {
      return;
    }
    isNewLine = endl.equals(value);
    writer.write(value);
  }

  public void flush() throws Exception {
    writer.flush();
  }

  public void close() throws Exception {
    writer.close();
  }
}
