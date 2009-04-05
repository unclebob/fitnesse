// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import util.RegexTestCase;

public class WikiPagePropertiesTest extends RegexTestCase {
  private WikiPageProperties properties;

  static String endl = System.getProperty("line.separator"),
    tab = "\t";
  static String sampleXml =
    "<?xml version=\"1.0\"?>" + endl +
      "<properties>" + endl +
      tab + "<Edit/>" + endl +
      tab + "<ParentOne>" + endl +
      tab + tab + "<ChildOne>child one value</ChildOne>" + endl +
      tab + "</ParentOne>" + endl +
      tab + "<ParentTwo value=\"parent 2 value\">" + endl +
      tab + tab + "<ChildTwo>child two value</ChildTwo>" + endl +
      tab + "</ParentTwo>" + endl +
      tab + "<SymbolicLinks>" + endl +
      tab + tab + "<BackLink>&lt;BackWard.SymLink</BackLink>" + endl +
      tab + tab + "<RelLink>RelaTive.SymLink</RelLink>" + endl +
      tab + tab + "<AbsLink>.AbsoLute.SymLink</AbsLink>" + endl +
      tab + tab + "<SubLink>&gt;SubChild.SymLink</SubLink>" + endl +
      tab + "</SymbolicLinks>" + endl +
      tab + "<Test/>" + endl +
      tab + "<VirtualWiki>http://someurl</VirtualWiki>" + endl +
      "</properties>" + endl;
  static String[] sampleXmlFragments = sampleXml.split("\t*" + endl);

  public void setUp() throws Exception {
    installPropertiesFrom(sampleXml);
  }

  private void installPropertiesFrom(String xmlSample) throws Exception {
    InputStream sampleInputStream = new ByteArrayInputStream(sampleXml.getBytes());
    properties = new WikiPageProperties(sampleInputStream);
  }

  public void tearDown() throws Exception {
  }

  public void testLoadingOfXmlWithoutAddedSpaces() throws Exception {
    validateLoading();
  }

  private void validateLoading() throws Exception {
    assertTrue(properties.has("Edit"));
    assertTrue(properties.has("Test"));
    assertFalse(properties.has("Suite"));
    assertEquals("http://someurl", properties.get("VirtualWiki"));

    WikiPageProperty parentOne = properties.getProperty("ParentOne");
    assertEquals(null, parentOne.getValue());
    assertEquals("child one value", parentOne.get("ChildOne"));

    WikiPageProperty parentTwo = properties.getProperty("ParentTwo");
    assertEquals("parent 2 value", parentTwo.getValue());
    assertEquals("child two value", parentTwo.get("ChildTwo"));

    WikiPageProperty symbolics = properties.getProperty("SymbolicLinks");
    assertEquals("<BackWard.SymLink", symbolics.get("BackLink"));
    assertEquals("RelaTive.SymLink", symbolics.get("RelLink"));
    assertEquals(".AbsoLute.SymLink", symbolics.get("AbsLink"));
    assertEquals(">SubChild.SymLink", symbolics.get("SubLink"));
  }

  public void testSave() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
    properties.save(os);

    String xml = os.toString();
    for (String fragment : sampleXmlFragments) {
      assertTrue(fragment, xml.contains(fragment));
    }
  }

  public void testKeySet() throws Exception {
    properties = new WikiPageProperties();
    properties.set("one");
    properties.set("two");
    properties.set("three");
    Set<?> keys = properties.keySet();

    assertTrue(keys.contains("one"));
    assertTrue(keys.contains("two"));
    assertTrue(keys.contains("three"));
    assertFalse(keys.contains("four"));
  }

  public void testIsSerializable() throws Exception {
    try {
      new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(properties);
    }
    catch (NotSerializableException e) {
      fail("its not serializable: " + e);
    }
  }

  public void testLastModificationTime() throws Exception {
    SimpleDateFormat format = WikiPageProperty.getTimeFormat();
    WikiPageProperties props = new WikiPageProperties();
    assertEquals(format.format(new Date()), format.format(props.getLastModificationTime()));
    Date date = format.parse("20040101000001");
    props.setLastModificationTime(date);
    assertEquals("20040101000001", props.get(PageData.PropertyLAST_MODIFIED));
    assertEquals(date, props.getLastModificationTime());
  }

  public void testShouldRemoveSpacesFromPropertyValues() throws Exception {
    String sampleXmlWithSpaces = sampleXml.replaceAll("</", " </");
    installPropertiesFrom(sampleXmlWithSpaces);
    validateLoading();
  }
}
