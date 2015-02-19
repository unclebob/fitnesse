// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting.history;

import fitnesse.FitNesseContext;
import fitnesse.reporting.BaseFormatter;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.Instruction;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.Expectation;
import fitnesse.testsystems.TableCell;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import fitnesse.util.TimeMeasurement;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TestXmlFormatter extends BaseFormatter implements Closeable {
  private final FitNesseContext context;
  private final WriterFactory writerFactory;
  private TimeMeasurement currentTestStartTime;
  private TimeMeasurement totalTimeMeasurement;
  private StringBuilder outputBuffer;
  protected final TestExecutionReport testResponse;
  public List<TestExecutionReport.InstructionResult> instructionResults = new ArrayList<TestExecutionReport.InstructionResult>();

  public TestXmlFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) {
    super(page);
    this.context = context;
    this.writerFactory = writerFactory;
    totalTimeMeasurement = new TimeMeasurement().start();
    testResponse = new TestExecutionReport(context.version, page.getPageCrawler().getFullPath().toString(), "" + context.port);
    resetTimer();
  }

  public long startedAt() {
    return totalTimeMeasurement.startedAt();
  }

  public long runTime() {
    return currentTestStartTime.elapsed();
  }

  @Override
  public void testStarted(WikiTestPage test) {
    resetTimer();
    appendHtmlToBuffer(WikiPageUtil.getHeaderPageHtml(getPage()));
  }

  @Override
  public void testOutputChunk(String output) {
    appendHtmlToBuffer(output);
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    if (testResult == null) {
      return;
    }
    Instruction instruction = assertion.getInstruction();
    Expectation expectation = assertion.getExpectation();
    TestExecutionReport.InstructionResult instructionResult = new TestExecutionReport.InstructionResult();
    instructionResults.add(instructionResult);

    String id = instruction.getId();

    instructionResult.instruction = instruction.toString();
    instructionResult.slimResult = testResult.toString();
    try {
      TestExecutionReport.Expectation expectationResult = new TestExecutionReport.Expectation();
      instructionResult.addExpectation(expectationResult);
      expectationResult.instructionId = id;
      expectationResult.type = expectation.getClass().getSimpleName();
      expectationResult.actual = testResult.getActual();
      expectationResult.expected = testResult.getExpected();
      expectationResult.evaluationMessage = testResult.getMessage();
      if (testResult.getExecutionResult() != null) {
        expectationResult.status = testResult.getExecutionResult().toString();
      }
      if (expectation instanceof TableCell) {
        TableCell cell = (TableCell) expectation;
        expectationResult.col = Integer.toString(cell.getCol());
        expectationResult.row = Integer.toString(cell.getRow());
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to process assertion " + assertion + " with test result " + testResult, e);
    }
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    Instruction instruction = assertion.getInstruction();
    Expectation expectation = assertion.getExpectation();
    TestExecutionReport.InstructionResult instructionResult = new TestExecutionReport.InstructionResult();
    instructionResults.add(instructionResult);

    String id = instruction.getId();

    instructionResult.instruction = instruction.toString();
    try {
      TestExecutionReport.Expectation expectationResult = new TestExecutionReport.Expectation();
      instructionResult.addExpectation(expectationResult);
      expectationResult.instructionId = id;
      expectationResult.type = expectation.getClass().getSimpleName();
      expectationResult.evaluationMessage = exceptionResult.getMessage();
      expectationResult.status = exceptionResult.getExecutionResult().toString();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to process assertion " + assertion + " with exception result " + exceptionResult, e);
    }
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    currentTestStartTime.stop();
    super.testComplete(test, testSummary);
    processTestResults(test, testSummary);
    testResponse.tallyPageCounts(ExecutionResult.getExecutionResult(test.getName(), testSummary));
  }

  public void processTestResults(final WikiTestPage testPage, TestSummary testSummary) {
    TestExecutionReport.TestResult currentResult = newTestResult();
    testResponse.addResult(currentResult);
    currentResult.startTime = currentTestStartTime.startedAt();
    currentResult.content = outputBuffer == null ? null : outputBuffer.toString();
    outputBuffer = null;
    addCountsToResult(currentResult, testSummary);
    currentResult.runTimeInMillis = String.valueOf(currentTestStartTime.elapsed());
    currentResult.relativePageName = testPage.getName();
    currentResult.tags = testPage.getData().getAttribute(PageData.PropertySUITES);
    currentResult.getInstructions().addAll(instructionResults);
    instructionResults = new ArrayList<TestExecutionReport.InstructionResult>();

  }

  protected TestExecutionReport.TestResult newTestResult() {
    return new TestExecutionReport.TestResult();
  }

  @Override
  public void close() throws IOException {
    setTotalRunTimeOnReport(totalTimeMeasurement);
    writeResults();
  }

  private void resetTimer() {
    currentTestStartTime = new TimeMeasurement().start();
  }

  protected void setTotalRunTimeOnReport(TimeMeasurement totalTimeMeasurement) {
    testResponse.setTotalRunTimeInMillis(totalTimeMeasurement);
  }

  protected void writeResults() throws IOException {
    writeResults(writerFactory.getWriter(context, getPage(), getPageCounts(), totalTimeMeasurement.startedAt()));
  }

  @Override
  public int getErrorCount() {
    return getPageCounts().getWrong() + getPageCounts().getExceptions();
  }

  protected void writeResults(Writer writer) throws IOException {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", testResponse);
    Template template = context.pageFactory.getVelocityEngine().getTemplate("testResults.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  protected TestSummary getPageCounts() {
    return testResponse.getFinalCounts();
  }

  private void addCountsToResult(TestExecutionReport.TestResult currentResult, TestSummary testSummary) {
    currentResult.right = Integer.toString(testSummary.getRight());
    currentResult.wrong = Integer.toString(testSummary.getWrong());
    currentResult.ignores = Integer.toString(testSummary.getIgnores());
    currentResult.exceptions = Integer.toString(testSummary.getExceptions());
  }

  private void appendHtmlToBuffer(String output) {
    if (outputBuffer == null)
      outputBuffer = new StringBuilder();
    outputBuffer.append(output);
  }

  public interface WriterFactory {
    Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException;
  }

}
