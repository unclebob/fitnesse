// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.Instruction;
import fitnesse.testrunner.CompositeExecutionLog;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.Expectation;
import fitnesse.testsystems.TableCell;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import util.TimeMeasurement;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class XmlFormatter extends BaseFormatter {
  public interface WriterFactory {
    Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException;
  }

  private WriterFactory writerFactory;
  private TimeMeasurement currentTestStartTime;
  protected TimeMeasurement totalTimeMeasurement;
  private StringBuilder outputBuffer;
  protected TestExecutionReport testResponse = new TestExecutionReport();
  public List<TestExecutionReport.InstructionResult> instructionResults = new ArrayList<TestExecutionReport.InstructionResult>();
  protected TestSummary finalSummary = new TestSummary();

  public XmlFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) {
    super(context, page);
    this.writerFactory = writerFactory;
    totalTimeMeasurement = new TimeMeasurement().start();
  }

  @Override
  public void testStarted(WikiTestPage test) {
    currentTestStartTime = new TimeMeasurement().start();
    appendHtmlToBuffer(WikiPageUtil.getHeaderPageHtml(getPage()));
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
  }

  @Override
  public void testOutputChunk(String output) {
    appendHtmlToBuffer(output);
  }

  // TODO: store tables -> need handler startNewTable(SlimTable slimTable)

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
    } catch (Throwable e) {
      e.printStackTrace();
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
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException {
    currentTestStartTime.stop();
    super.testComplete(test, testSummary);
    processTestResults(test.getName(), testSummary, currentTestStartTime);
  }

  public void processTestResults(final String relativeTestName, TestSummary testSummary, TimeMeasurement notUsed) {
    finalSummary = new TestSummary(testSummary);
    TestExecutionReport.TestResult currentResult = newTestResult();
    testResponse.results.add(currentResult);
    currentResult.startTime = currentTestStartTime.startedAt();
    currentResult.content = outputBuffer == null ? null : outputBuffer.toString();
    outputBuffer = null;
    addCountsToResult(currentResult, testSummary);
    currentResult.runTimeInMillis = String.valueOf(currentTestStartTime.elapsed());
    currentResult.relativePageName = relativeTestName;
    currentResult.tags = page.readOnlyData().getAttribute(PageData.PropertySUITES);
    currentResult.getInstructions().addAll(instructionResults);
    instructionResults = new ArrayList<TestExecutionReport.InstructionResult>();

  }

  protected TestExecutionReport.TestResult newTestResult() {
    return new TestExecutionReport.TestResult();
  }

  protected void setPage(WikiPage testPage) {
    this.page = testPage;
    testResponse.rootPath = testPage.getName();
  }

  @Override
  public void close() throws IOException {
    super.close();
    setTotalRunTimeOnReport(totalTimeMeasurement);
    writeResults();
  }

  protected void setTotalRunTimeOnReport(TimeMeasurement totalTimeMeasurement) {
    testResponse.setTotalRunTimeInMillis(totalTimeMeasurement);
  }

  protected void writeResults() throws IOException {
    writeResults(writerFactory.getWriter(context, getPageForHistory(), finalSummary, currentTestStartTime.startedAt()));
  }

  protected WikiPage getPageForHistory() {
    return page;
  }

  @Override
  public int getErrorCount() {
    return finalSummary.wrong + finalSummary.exceptions;
  }

  protected void writeResults(Writer writer) throws IOException {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", testResponse);
    Template template = context.pageFactory.getVelocityEngine().getTemplate("testResults.vm");
    template.merge(velocityContext, writer);
    writer.close();
  }

  protected TestSummary getFinalSummary() {
    return finalSummary;
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

}
