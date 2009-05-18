// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import util.RegexTestCase;
import fit.Counts;
import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.runner.HtmlResultFormatter;
import fitnesse.runner.MockResultFormatter;
import fitnesse.runner.PageResult;
import fitnesse.runner.XmlResultFormatter;

public class TestResultFormattingResponderTest extends RegexTestCase {
  private PipedOutputStream output;
  private PipedInputStream input;
  private TestResultFormattingResponder responder;
  private MockResultFormatter formatter;
  private PageResult result1;
  private PageResult result2;
  private FitNesseContext context;

  public void setUp() throws Exception {
    output = new PipedOutputStream();
    input = new PipedInputStream(output);

    responder = new TestResultFormattingResponder();
    formatter = new MockResultFormatter();
    responder.formatter = formatter;

    result1 = new PageResult("Result1Title", new TestSummary(1, 2, 3, 4), "result1 data");
    result2 = new PageResult("Result2Title", new TestSummary(4, 3, 2, 1), "result2 data");

    context = new FitNesseContext();
  }

  public void testOneResult() throws Exception {
    FitProtocol.writeData(result1.toString(), output);
    FitProtocol.writeCounts(new Counts(0, 0, 0, 0), output);
    responder.processResults(input);

    assertEquals(1, formatter.results.size());
    assertEquals(result1.toString(), formatter.results.get(0).toString());
  }

  public void testTwoResults() throws Exception {
    FitProtocol.writeData(result1.toString(), output);
    FitProtocol.writeData(result2.toString(), output);
    FitProtocol.writeCounts(new Counts(0, 0, 0, 0), output);
    responder.processResults(input);

    assertEquals(2, formatter.results.size());
    assertEquals(result1.toString(), formatter.results.get(0).toString());
    assertEquals(result2.toString(), formatter.results.get(1).toString());
  }

  public void testFinalCounts() throws Exception {
    FitProtocol.writeData(result1.toString(), output);
    Counts counts = new Counts(1, 2, 3, 4);
    FitProtocol.writeCounts(counts, output);
    responder.processResults(input);

    TestSummary summary = formatter.finalSummary;
    assertEquals(counts.right, summary.getRight());
    assertEquals(counts.wrong, summary.getWrong());
    assertEquals(counts.ignores, summary.getIgnores());
    assertEquals(counts.exceptions, summary.getExceptions());
  }

  public void testMakeResponse() throws Exception {
    MockRequest request = new MockRequest();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    FitProtocol.writeData(result1.toString(), output);
    FitProtocol.writeData(result2.toString(), output);
    FitProtocol.writeCounts(new Counts(5, 5, 5, 5), output);
    request.addInput("results", output.toString());

    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String content = sender.sentData();

    assertSubString("Mock Results", content);
  }

  public void testMockFormatter() throws Exception {
    checkFormatterCreated(null, MockResultFormatter.class);
  }

  public void testHtmlFormatter() throws Exception {
    checkFormatterCreated("html", HtmlResultFormatter.class);
  }

  public void testXmlFormatter() throws Exception {
    checkFormatterCreated("xml", XmlResultFormatter.class);
  }

  private void checkFormatterCreated(String format, Class<?> formatterClass) throws Exception {
    MockRequest request = new MockRequest();
    request.addHeader("Host", "locahost:8080");
    request.setResource("/");
    if (format != null)
      request.addInput("format", format);
    responder.init(context, request);
    assertEquals(formatterClass, responder.formatter.getClass());
  }
}
