// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.XmlUtil;
import util.XmlWriter;
import fitnesse.components.ContentBuffer;
import fitnesse.responders.run.TestSummary;

public class XmlResultFormatter implements ResultFormatter {
  private ContentBuffer buffer;
  private Document document;
  private boolean closed = false;
  private byte[] tailBytes;

  public XmlResultFormatter(String host, String rootPath) throws IOException {
    buffer = new ContentBuffer(".xml");
    createDocument(host, rootPath);
    writeDocumentHeader();
  }

  private void writeDocumentHeader() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    XmlWriter writer = new XmlWriter(output);
    writer.write(document);
    writer.close();
    String xmlText = output.toString();
    int endIndex = xmlText.indexOf("</testResults>");
    String head = xmlText.substring(0, endIndex);
    String tail = xmlText.substring(endIndex);
    tailBytes = tail.getBytes();
    buffer.append(head);
  }

  private void createDocument(String host, String rootPath) {
    document = XmlUtil.newDocument();
    Element root = document.createElement("testResults");
    document.appendChild(root);
    XmlUtil.addTextNode(document, root, "host", host);
    XmlUtil.addTextNode(document, root, "rootPath", rootPath);
  }

  public void acceptResult(PageResult result) throws IOException {
    Element resultElement = document.createElement("result");
    XmlUtil.addTextNode(document, resultElement, "relativePageName", result.title());
    XmlUtil.addCdataNode(document, resultElement, "content", result.content());
    resultElement.appendChild(makeCountsElement("counts", result.testSummary()));
    writeElement(resultElement);
  }

  public void acceptFinalCount(TestSummary testSummary) throws IOException {
    Element countsElement = makeCountsElement("finalCounts", testSummary);
    writeElement(countsElement);
  }

  public int getByteCount() throws IOException {
    close();
    return buffer.getSize();
  }

  public InputStream getResultStream() throws IOException {
    close();
    return buffer.getInputStream();
  }

  private void close() throws IOException {
    if (!closed) {
      buffer.append(tailBytes);
      closed = true;
    }
  }

  private void writeElement(Element resultElement) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    XmlWriter writer = new XmlWriter(output);
    writer.write(resultElement, 1);
    writer.close();
    buffer.append(output.toByteArray());
  }

  private Element makeCountsElement(String name, TestSummary testSummary) {
    Element countsElement = document.createElement(name);
    XmlUtil.addTextNode(document, countsElement, "right", testSummary.getRight() + "");
    XmlUtil.addTextNode(document, countsElement, "wrong", testSummary.getWrong() + "");
    XmlUtil.addTextNode(document, countsElement, "ignores", testSummary.getIgnores() + "");
    XmlUtil.addTextNode(document, countsElement, "exceptions", testSummary.getExceptions() + "");
    return countsElement;
  }
}
