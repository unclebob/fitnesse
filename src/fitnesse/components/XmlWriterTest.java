// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import junit.framework.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import fitnesse.components.XmlWriter;

public class XmlWriterTest extends TestCase
{
	private ByteArrayOutputStream output;
	private Document doc;

	static String sampleXml = null;

	static
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(out);
		writer.println("<?xml version=\"1.0\"?>");
		writer.println("<rootElement version=\"2.0\">");
		writer.println("\t<emptytElement dire=\"straights\" strawberry=\"alarmclock\"/>");
		writer.println("\t<fullElement>");
		writer.println("\t\t<childElement/>");
		writer.println("\t</fullElement>");
		writer.println("\t<text>some text</text>");
		writer.println("\t<cdata><![CDATA[<>&;]]></cdata>");
		writer.println("</rootElement>");
		writer.flush();
		sampleXml = new String(out.toByteArray());
	}

	public void setUp() throws Exception
	{
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.parse(new ByteArrayInputStream(sampleXml.getBytes()));
		output = new ByteArrayOutputStream();
	}

	public void tearDown() throws Exception
	{
	}

	public void testAll() throws Exception
	{
		XmlWriter writer = new XmlWriter(output);
		writer.write(doc);
		writer.flush();
		writer.close();

		String results = new String(output.toByteArray());
		assertEquals(sampleXml, results);
	}
}
