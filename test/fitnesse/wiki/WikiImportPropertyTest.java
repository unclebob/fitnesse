// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import fitnesse.util.Clock;

public class WikiImportPropertyTest {
  private WikiImportProperty property;

  @Before
  public void setUp() {
    property = new WikiImportProperty("");
  }

  @Test
  public void testSource() throws Exception {
    property = new WikiImportProperty("import source");
    assertEquals("import source", property.getSourceUrl());
    assertEquals("import source", property.get("Source"));
  }

  @Test
  public void testIsRoot() throws Exception {
    assertFalse(property.isRoot());
    assertFalse(property.has("IsRoot"));

    property.setRoot(true);

    assertTrue(property.isRoot());
    assertTrue(property.has("IsRoot"));
  }

  @Test
  public void testAutoUpdate() throws Exception {
    assertFalse(property.isAutoUpdate());
    assertFalse(property.has("AutoUpdate"));

    property.setAutoUpdate(true);

    assertTrue(property.isAutoUpdate());
    assertTrue(property.has("AutoUpdate"));
  }

  @Test
  public void testLastUpdated() throws Exception {
    DateFormat format = WikiPageProperty.getTimeFormat();
    Date date = Clock.currentDate();
    property.setLastRemoteModificationTime(date);

    assertEquals(format.format(date), format.format(property.getLastRemoteModificationTime()));

    assertEquals(format.format(date), property.get("LastRemoteModification"));
  }

  @Test
  public void testFailedCreateFromProperty() throws Exception {
    assertNull(WikiImportProperty.createFrom(new WikiPageProperty()));
  }

  @Test
  public void testCreateFromProperty() throws Exception {
    WikiPageProperty rawImportProperty = property.set(WikiImportProperty.PROPERTY_NAME);
    rawImportProperty.set("IsRoot");
    rawImportProperty.set("AutoUpdate");
    rawImportProperty.set("Source", "some source");
    Date date = Clock.currentDate();
    rawImportProperty.set("LastRemoteModification", WikiPageProperty.getTimeFormat().format(date));

    WikiImportProperty importProperty = WikiImportProperty.createFrom(property);
    assertEquals("some source", importProperty.getSourceUrl());
    assertTrue(importProperty.isRoot());
    assertTrue(importProperty.isAutoUpdate());
    DateFormat format = WikiPageProperty.getTimeFormat();
    assertEquals(format.format(date), format.format(importProperty.getLastRemoteModificationTime()));
  }

  @Test
  public void testAddtoProperty() throws Exception {
    WikiImportProperty importProperty = new WikiImportProperty("some source");
    importProperty.setRoot(true);
    importProperty.setAutoUpdate(true);
    importProperty.addTo(property);

    WikiImportProperty importProperty2 = WikiImportProperty.createFrom(property);
    assertEquals("some source", importProperty2.getSourceUrl());
    assertTrue(importProperty2.isRoot());
    assertTrue(importProperty2.isAutoUpdate());
  }

}
