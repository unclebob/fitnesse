// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertSubString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import util.FileUtil;

public class InputStreamResponseTest implements ResponseSender {
  private InputStreamResponse response;
  private boolean closed = false;
  private ByteArrayOutputStream output;
  private File testFile = new File("testFile.test");
  private long bytesSent = 0;

  @Before
  public void setUp() throws Exception {
    response = new InputStreamResponse();
    output = new ByteArrayOutputStream();
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFile(testFile);
  }

  @Test
  public void testSimpleUsage() throws Exception {
    ByteArrayInputStream input = new ByteArrayInputStream("content".getBytes());
    response.setBody(input, 7);
    response.sendTo(this);
    assertTrue(closed);

    ResponseParser result = new ResponseParser(new ByteArrayInputStream(output.toByteArray()));
    assertEquals(200, result.getStatus());
    assertEquals(7 + "", result.getHeader("Content-Length"));
    assertEquals("content", result.getBody());
  }

  @Test
  public void testWithFile() throws Exception {
    FileUtil.createFile(testFile, "content");
    response.setBody(testFile);
    response.sendTo(this);
    assertTrue(closed);

    ResponseParser result = new ResponseParser(new ByteArrayInputStream(output.toByteArray()));
    assertEquals(200, result.getStatus());
    assertEquals(7 + "", result.getHeader("Content-Length"));
    assertEquals("content", result.getBody());
  }

  @Test
  public void testWithLargeFile() throws Exception {
    writeLinesToFile(1000);

    response.setBody(testFile);
    response.sendTo(this);
    String responseString = output.toString();
    assertSubString("Content-Length: 100000", responseString);
    assertTrue(bytesSent > 100000);
  }

  @Test
  public void testWithLargerFile() throws Exception {
    writeLinesToFile(100000);

    response.setBody(testFile);
    response.sendTo(this);
    String responseString = output.toString();
    assertSubString("Content-Length: 10000000", responseString);
    assertTrue(bytesSent > 10000000);
  }

  @Test
  @Ignore("Don't run unless you have some time to kill.")
  public void testWithReallyBigFile() throws Exception {
    writeLinesToFile(10000000);

    response.setBody(testFile);
    response.sendTo(this);
    String responseString = output.toString();
    assertSubString("Content-Length: 1000000000", responseString);
    assertTrue(bytesSent > 1000000000);
  }

  private void writeLinesToFile(int lines) throws IOException {
    String sampleLine = "This is a sample line of a large file that's created for the large file tests. It's 100 bytes long.\n";
    byte[] bytes = sampleLine.getBytes();
    FileOutputStream testFileOutput = new FileOutputStream(testFile);
    for (int i = 0; i < lines; i++)
      testFileOutput.write(bytes);
    testFileOutput.close();
  }

  @Override
  public void send(byte[] bytes) {
    if (bytesSent < 500)
      try {
        output.write(bytes);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    bytesSent += bytes.length;
  }

  @Override
  public void close() {
    closed = true;
  }
}
