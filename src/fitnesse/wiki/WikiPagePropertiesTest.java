// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.io.*;
import java.util.*;
import fitnesse.testutil.RegexTest;

public class WikiPagePropertiesTest extends RegexTest
{
	private InputStream sampleInputStream;
	private WikiPageProperties properties;

	static String endl = System.getProperty("line.separator");
	static String sampleXml = "<?xml version=\"1.0\"?>" + endl +
	  "<properties>" + endl +
	  "\t<Edit/>" + endl +
	  "\t<Test/>" + endl +
	  "\t<VirtualWiki>http://someurl</VirtualWiki>" + endl +
	  "</properties>" + endl;

	public void setUp() throws Exception
	{
		sampleInputStream = new ByteArrayInputStream(sampleXml.getBytes());
		properties = new WikiPageProperties(sampleInputStream);
	}

	public void tearDown() throws Exception
	{
	}

	public void testLoading() throws Exception
	{
		assertTrue(properties.has("Edit"));
		assertTrue(properties.has("Test"));
		assertFalse(properties.has("Suite"));
		assertEquals("http://someurl", properties.get("VirtualWiki"));
	}

	public void testSave() throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
		properties.save(os);

		String xml = os.toString();
		assertEquals(sampleXml, xml);
	}

	public void testSetProperty() throws Exception
	{
		properties = new WikiPageProperties();
		properties.set("newProperty1");
		assertEquals(true, properties.has("newProperty1"));
		assertEquals("true", properties.get("newProperty1"));

		properties.set("newProperty2", "some value");
		assertEquals("some value", properties.get("newProperty2"));
	}

	public void testRemoveProperty() throws Exception
	{
		properties = new WikiPageProperties();
		properties.set("newProperty", "blah");
		properties.remove("newProperty");
		assertEquals(false, properties.has("newProperty"));
	}

	public void testKeySet() throws Exception
	{
		properties = new WikiPageProperties();
		properties.set("one");
		properties.set("two");
		properties.set("three");
		Set keys = properties.keySet();

		assertTrue(keys.contains("one"));
		assertTrue(keys.contains("two"));
		assertTrue(keys.contains("three"));
		assertFalse(keys.contains("four"));
	}

	public void testIsSerializable() throws Exception
	{
		try
		{
			new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(properties);
		}
		catch(NotSerializableException e)
		{
			fail("its not serializabl: " + e);
		}
	}

	public void testHas() throws Exception
	{
		properties.set("true", "true");
		properties.set("false", "false");

		assertTrue(properties.has("true"));
		assertTrue(properties.has("false"));
	}

	public void testConstructorTakingAMap() throws Exception
	{
		HashMap map = new HashMap();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "false");
		properties = new WikiPageProperties(map);
		assertEquals("value1", properties.get("key1"));
		assertEquals(true, properties.has("key2"));
		assertFalse(properties.has("key3"));
	}

	public void testSymbolicLinks() throws Exception
	{
		properties.addSymbolicLink("LinkName", PathParser.parse("PatH.ToThe.PagE"));
		assertTrue(properties.hasSymbolicLink("LinkName"));
		assertFalse(properties.hasSymbolicLink("SomeOtherLink"));

		assertEquals(PathParser.parse("PatH.ToThe.PagE"), properties.getSymbolicLink("LinkName"));
		assertEquals(null, properties.getSymbolicLink("SomeOtherLink"));

		Set linkNames = properties.getSymbolicLinkNames();
		assertEquals(1, linkNames.size());
		assertEquals("LinkName", linkNames.iterator().next());

		properties.removeSymbolicLink("LinkName");
		assertFalse(properties.hasSymbolicLink("LinkName"));
	}

	public void testSymbolicLinksSave() throws Exception
	{
		ByteArrayOutputStream output = saveSomeSymbolicLinks();
		String xml = output.toString();

		assertHasRegexp("<symbolicLink>\\s*<name>LinkOne</name>\\s*<path>PatH.OnE</path>\\s*</symbolicLink>", xml);
		assertHasRegexp("<symbolicLink>\\s*<name>LinkTwo</name>\\s*<path>PatH.TwO</path>\\s*</symbolicLink>", xml);
	}

	private ByteArrayOutputStream saveSomeSymbolicLinks() throws Exception
	{
		properties.addSymbolicLink("LinkOne", PathParser.parse("PatH.OnE"));
		properties.addSymbolicLink("LinkTwo", PathParser.parse("PatH.TwO"));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		properties.save(output);
		return output;
	}

	public void testSymbolicLinksLoading() throws Exception
	{
		ByteArrayOutputStream output = saveSomeSymbolicLinks();
		ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
		WikiPageProperties newProperties = new WikiPageProperties(input);

		assertTrue(newProperties.hasSymbolicLink("LinkOne"));
		assertEquals(PathParser.parse("PatH.OnE"), newProperties.getSymbolicLink("LinkOne"));
		assertTrue(newProperties.hasSymbolicLink("LinkTwo"));
		assertEquals(PathParser.parse("PatH.TwO"), newProperties.getSymbolicLink("LinkTwo"));
	}
}
