// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import fitnesse.testutil.RegexTest;
import fitnesse.util.*;
import org.w3c.dom.*;

import java.io.InputStream;

public class XmlResultFormatterTest extends RegexTest
{
	private XmlResultFormatter formatter;
	private PageResult result1;
	private PageResult result2;
	private Counts finalCounts;

	public void setUp() throws Exception
	{
		formatter = new XmlResultFormatter("localhost:8081", "RootPath");
		result1 = new PageResult("ResultOne", new Counts(1, 2, 3, 4), "result one content");
		result2 = new PageResult("ResultTwo", new Counts(4, 3, 2, 1), "result two content");
		finalCounts = new Counts(5, 5, 5, 5);
	}

	public void tearDown() throws Exception
	{
		formatter = null;
		System.gc();
	}

	public void testValidXml() throws Exception
	{
		Document doc = getXmlDoc();
		Element documentElement = doc.getDocumentElement();
		assertEquals("testResults", documentElement.getNodeName());
		assertEquals("localhost:8081", XmlUtil.getTextValue(documentElement, "host"));
		assertEquals("RootPath", XmlUtil.getTextValue(documentElement, "rootPath"));
	}

	public void testOneResult() throws Exception
	{
		formatter.acceptResult(result1);
		Document doc = getXmlDoc();
		NodeList results = doc.getElementsByTagName("result");
		assertEquals(1, results.getLength());
		Element result = (Element) results.item(0);
		checkResultElement(result, result1);
	}

	public void testTwoResults() throws Exception
	{
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

	public void testFinalCounts() throws Exception
	{
		formatter.acceptFinalCount(finalCounts);
		Document doc = getXmlDoc();
		NodeList finalCountsList = doc.getElementsByTagName("finalCounts");
		assertEquals(1, finalCountsList.getLength());
		Element finalCountElement = (Element) finalCountsList.item(0);
		checkCounts(finalCounts, finalCountElement);
	}

	private void checkResultElement(Element resultElement, PageResult result) throws Exception
	{
		assertEquals(result.title(), XmlUtil.getTextValue(resultElement, "relativePageName"));
		assertEquals(result.content(), XmlUtil.getTextValue(resultElement, "content"));
		Element countsElement = XmlUtil.getElementByTagName(resultElement, "counts");
		Counts counts = result.counts();
		checkCounts(counts, countsElement);
	}

	private void checkCounts(Counts counts, Element countsElement) throws Exception
	{
		assertEquals(counts.right + "", XmlUtil.getTextValue(countsElement, "right"));
		assertEquals(counts.wrong + "", XmlUtil.getTextValue(countsElement, "wrong"));
		assertEquals(counts.ignores + "", XmlUtil.getTextValue(countsElement, "ignores"));
		assertEquals(counts.exceptions + "", XmlUtil.getTextValue(countsElement, "exceptions"));
	}

	private String getXml() throws Exception
	{
		InputStream input = formatter.getResultStream();
		assertNotNull(input);

		int bytes = formatter.getByteCount();
		String xml = new StreamReader(input).read(bytes);
		return xml;
	}

	private Document getXmlDoc() throws Exception
	{
		return XmlUtil.newDocument(getXml());
	}
}
