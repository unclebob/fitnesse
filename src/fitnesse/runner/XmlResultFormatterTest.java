// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import util.RegexTestCase;
import util.StreamReader;
import util.XmlUtil;
import fitnesse.responders.run.TestSummary;

public class XmlResultFormatterTest extends RegexTestCase {
  private XmlResultFormatter formatter;
  private PageResult result1;
  private PageResult result2;
  private TestSummary finalSummary;

  public void setUp() throws Exception {
    formatter = new XmlResultFormatter("localhost:8081", "RootPath");
    result1 = new PageResult("ResultOne", new TestSummary(1, 2, 3, 4), "result one content");
    result2 = new PageResult("ResultTwo", new TestSummary(4, 3, 2, 1), "result two content");
    finalSummary = new TestSummary(5, 5, 5, 5);
  }

  public void tearDown() throws Exception {
    formatter = null;
    System.gc();
  }

  public void testValidXml() throws Exception {
    Document doc = getXmlDoc();
    Element documentElement = doc.getDocumentElement();
    assertEquals("testResults", documentElement.getNodeName());
    assertEquals("localhost:8081", XmlUtil.getTextValue(documentElement, "host"));
    assertEquals("RootPath", XmlUtil.getTextValue(documentElement, "rootPath"));
  }

  public void testOneResult() throws Exception {
    formatter.acceptResult(result1);
    Document doc = getXmlDoc();
    NodeList results = doc.getElementsByTagName("result");
    assertEquals(1, results.getLength());
    Element result = (Element) results.item(0);
    checkResultElement(result, result1);
  }

  public void testTwoResults() throws Exception {
    formatter.acceptResult(result1);
    formatter.acceptResult(result2);
    Document doc = getXmlDoc();
    NodeList results = doc.getElementsByTagName("result");
    assertEquals(2, results.getLength());
    Element resultElement1 = (Element) results.item(0);
    Element resultElement2 = (Element) results.item(1);

    checkResultElement(resultElement1, result1);
    checkResultElement(resultElement2, result2);
  }

  public void testFinalCounts() throws Exception {
    formatter.acceptFinalCount(finalSummary);
    Document doc = getXmlDoc();
    NodeList finalCountsList = doc.getElementsByTagName("finalCounts");
    assertEquals(1, finalCountsList.getLength());
    Element finalCountElement = (Element) finalCountsList.item(0);
    checkCounts(finalSummary, finalCountElement);
  }

  private void checkResultElement(Element resultElement, PageResult result) throws Exception {
    assertEquals(result.title(), XmlUtil.getTextValue(resultElement, "relativePageName"));
    assertEquals(result.content(), XmlUtil.getTextValue(resultElement, "content"));
    Element countsElement = XmlUtil.getElementByTagName(resultElement, "counts");
    TestSummary testSummary = result.testSummary();
    checkCounts(testSummary, countsElement);
  }

  private void checkCounts(TestSummary testSummary, Element countsElement) throws Exception {
    assertEquals(testSummary.getRight() + "", XmlUtil.getTextValue(countsElement, "right"));
    assertEquals(testSummary.getWrong() + "", XmlUtil.getTextValue(countsElement, "wrong"));
    assertEquals(testSummary.getIgnores() + "", XmlUtil.getTextValue(countsElement, "ignores"));
    assertEquals(testSummary.getExceptions() + "", XmlUtil.getTextValue(countsElement, "exceptions"));
  }

  private String getXml() throws Exception {
    InputStream input = formatter.getResultStream();
    assertNotNull(input);

    int bytes = formatter.getByteCount();
    String xml = new StreamReader(input).read(bytes);
    return xml;
  }

  private Document getXmlDoc() throws Exception {
    return XmlUtil.newDocument(getXml());
  }
}
