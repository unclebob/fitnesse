// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.Serializable;

import junit.framework.TestCase;

public class WikiPagePropertyTest extends TestCase {
  private WikiPageProperty property;

  public void setUp() throws Exception {
    property = new WikiPageProperty("some value");
  }

  public void tearDown() throws Exception {
  }

  public void testIsSerializable() throws Exception {
    assertTrue(property instanceof Serializable);
  }

  public void testValue() throws Exception {
    assertEquals("some value", property.getValue());
  }

  public void testChildProperties() throws Exception {
    property.set("child1", new WikiPageProperty("child one value"));
    property.set("child2", new WikiPageProperty("child two value"));

    WikiPageProperty child1 = property.getProperty("child1");
    WikiPageProperty child2 = property.getProperty("child2");

    assertNotNull(child1);
    assertNotNull(child2);

    assertEquals("child one value", child1.getValue());
    assertEquals("child two value", child2.getValue());
  }

  public void testGetPropertyWhenThereAreNone() throws Exception {
    assertNull(property.getProperty("blah"));
  }

  public void testHasProperty() throws Exception {
    assertFalse(property.has("child"));

    property.set("child", new WikiPageProperty("child value"));

    assertTrue(property.has("child"));
  }

  public void testSetChildWithString() throws Exception {
    property.set("child", "child value");

    WikiPageProperty child = property.getProperty("child");

    assertNotNull(child);
    assertEquals("child value", child.getValue());
  }

  public void testSetBoolean() throws Exception {
    property.set("child");

    WikiPageProperty child = property.getProperty("child");

    assertNotNull(child);
    assertEquals(null, child.getValue());
  }

  public void testRemovePropety() throws Exception {
    property.set("child", "value");
    assertTrue(property.has("child"));

    property.remove("child");

    assertFalse(property.has("child"));
    assertNull(property.getProperty("child"));
  }

  public void testGetValueOfChild() throws Exception {
    property.set("child", "value");

    assertEquals("value", property.get("child"));
  }
}
