// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertSubString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import fitnesse.http.Request;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class FormattingOptionTest {
  private ByteArrayOutputStream output;
  private FormattingOption option;
  private CachingResultFormatter formatter;
  private PageResult result1;
  private PageResult result2;
  private int port = FitNesseUtil.PORT;

  @Before
  public void setUp() throws Exception {
    output = new ByteArrayOutputStream();
  }

  @After
  public void tearDown() throws Exception {
    new File("testOutput.txt").delete();
  }

  @Test
  public void testConstruction() throws Exception {
    option = new FormattingOption("mock", "stdout", output, "localhost", 8081, "SomePage");
    assertEquals("mock", option.format);
    assertSame(output, option.output);
    assertEquals("localhost", option.host);
    assertEquals(8081, option.port);
    assertEquals("SomePage", option.rootPath);
  }

  @Test
  public void testConstructionWithFile() throws Exception {
    option = new FormattingOption("mock", "testOutput.txt", output, "localhost", 8081, "SomePage");
    assertEquals(FileOutputStream.class, option.output.getClass());
    option.output.write("sample data".getBytes());
    option.output.close();
    assertEquals("sample data", FileUtil.getFileContent("testOutput.txt"));
  }

  @Test
  public void testRawResults() throws Exception {
    sampleFormatter();
    option = new FormattingOption("raw", "stdout", output, "localhost", port, "SomePage");
    option.process(formatter.getResultStream(), formatter.getByteCount());
    String content = output.toString();
    assertSubString(result1.toString(), content);
    assertSubString(result2.toString(), content);
  }

  @Test
  public void testRequest() throws Exception {
    option = new FormattingOption("mock", "stdout", output, "localhost", 8081, "SomePage");
    String requestString = option.buildRequest(new ByteArrayInputStream("test results".getBytes()), 12).getText();

    Request request = new Request(new ByteArrayInputStream(requestString.getBytes()));
    request.parse();
    assertEquals("POST /SomePage HTTP/1.1", request.getRequestLine());
    assertTrue(request.getHeader("Content-Type").toString().startsWith("multipart"));
    assertEquals("localhost:8081", request.getHeader("Host"));
    assertEquals("format", request.getInput("responder"));
    assertEquals("mock", request.getInput("format"));
    assertEquals("test results", request.getInput("results"));
  }

  @Test
  public void testTheWholeDeal() throws Exception {
    sampleFormatter();

    FitNesseUtil.startFitnesse(InMemoryPage.makeRoot("RooT"));
    try {
      option = new FormattingOption("mock", "stdout", output, "localhost", port, "");
      option.process(formatter.getResultStream(), formatter.getByteCount());
    }
    finally {
      FitNesseUtil.stopFitnesse();
    }

    String result = output.toString();
    assertSubString("Mock Results", result);
    assertSubString(result1.toString(), result);
    assertSubString(result2.toString(), result);
  }

  private void sampleFormatter() throws Exception {
    formatter = new CachingResultFormatter();
    result1 = new PageResult("ResultOne", new TestSummary(1, 2, 3, 4), "result one content");
    result2 = new PageResult("ResultTwo", new TestSummary(4, 3, 2, 1), "result two content");
    TestSummary finalSummary = new TestSummary(5, 5, 5, 5);
    formatter.acceptResult(result1);
    formatter.acceptResult(result2);
    formatter.acceptFinalCount(finalSummary);
  }
}
