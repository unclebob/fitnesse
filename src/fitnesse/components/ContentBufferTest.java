// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import util.StreamReader;

public class ContentBufferTest {

  @Test
  public void testName() throws Exception {
    String name = new ContentBuffer().getFile().getName();
    assertTrue(name.startsWith("FitNesse-"));
    assertTrue(name.endsWith(".tmp"));

    name = new ContentBuffer(".html").getFile().getName();
    assertTrue(name.startsWith("FitNesse-"));
    assertTrue(name.endsWith(".html"));
  }

  @Test
  public void testSimpleUsage() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    buffer.append("some content");
    assertEquals("some content", buffer.getContent());
  }

  @Test
  public void testGettingInputStream() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    buffer.append("some content");

    int bytes = buffer.getSize();
    assertEquals(12, bytes);

    InputStream input = buffer.getInputStream();
    String content = new StreamReader(input).read(12);
    assertEquals("some content", content);
  }

  @Test
  public void testDelete() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    File file = buffer.getFile();

    assertTrue(file.exists());
    buffer.delete();
    assertFalse(file.exists());
  }

  @Test
  public void testUnicode() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    buffer.append("??\uFFFD\uFFFD");
    assertEquals("??\uFFFD\uFFFD", new StreamReader(buffer.getInputStream()).read(buffer.getSize()));
  }
}
