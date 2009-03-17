// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;
import util.StreamReader;

public class ContentBufferTest extends TestCase {
  protected void tearDown() throws Exception {
    System.gc();
  }

  public void testName() throws Exception {
    String name = new ContentBuffer().getFile().getName();
    assertTrue(name.startsWith("FitNesse-"));
    assertTrue(name.endsWith(".tmp"));

    name = new ContentBuffer(".html").getFile().getName();
    assertTrue(name.startsWith("FitNesse-"));
    assertTrue(name.endsWith(".html"));
  }

  public void testSimpleUsage() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    buffer.append("some content");
    assertEquals("some content", buffer.getContent());
  }

  public void testGettingInputStream() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    buffer.append("some content");

    int bytes = buffer.getSize();
    assertEquals(12, bytes);

    InputStream input = buffer.getInputStream();
    String content = new StreamReader(input).read(12);
    assertEquals("some content", content);
  }

  public void testDelete() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    File file = buffer.getFile();

    assertTrue(file.exists());
    buffer.delete();
    assertFalse(file.exists());
  }

  public void testUnicode() throws Exception {
    ContentBuffer buffer = new ContentBuffer();
    buffer.append("??¾š");
    assertEquals("??¾š", new StreamReader(buffer.getInputStream()).read(buffer.getSize()));
  }
}
