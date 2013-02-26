// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.testsystems.TestPage;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.testsystems.slim.tables.Expectation;
import fitnesse.testsystems.slim.tables.SlimTable;
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
  private long currentTestStartTime;
  private StringBuilder outputBuffer;
  protected TestExecutionReport testResponse = new TestExecutionReport();
  public List<TestExecutionReport.InstructionResult> instructionResults = new ArrayList<TestExecutionReport.InstructionResult>();
  protected TestSummary finalSummary = new TestSummary();

  public XmlFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) {
    super(context, page);
    this.writerFactory = writerFactory;
  }

  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) {
    currentTestStartTime = timeMeasurement.startedAt();
    appendHtmlToBuffer(WikiPageUtil.getHeaderPageHtml(getPage()));
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
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
      if (expectation instanceof SlimTable.RowExpectation) {
        SlimTable.RowExpectation rowExpectation = (SlimTable.RowExpectation) expectation;
        expectationResult.col = Integer.toString(rowExpectation.getCol());
        expectationResult.row = Integer.toString(rowExpectation.getRow());
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
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
    super.testComplete(test, testSummary, timeMeasurement);
    processTestResults(test.getName(), testSummary, timeMeasurement);
  }

  public void processTestResults(final String relativeTestName, TestSummary testSummary, TimeMeasurement timeMeasurement) {
    finalSummary = new TestSummary(testSummary);
    TestExecutionReport.TestResult currentResult = newTestResult();
    testResponse.results.add(currentResult);
    currentResult.startTime = currentTestStartTime;
    currentResult.content = outputBuffer == null ? null : outputBuffer.toString();
    outputBuffer = null;
    addCountsToResult(currentResult, testSummary);
    currentResult.runTimeInMillis = String.valueOf(timeMeasurement.elapsed());
    currentResult.relativePageName = relativeTestName;
    currentResult.tags = page.readOnlyData().getAttribute(PageData.PropertySUITES);
    currentResult.getInstructions().addAll(instructionResults);
    instructionResults = new ArrayList<TestExecutionReport.InstructionResult>();

  }

  protected TestExecutionReport.TestResult newTestResult() {
    return new TestExecutionReport.TestResult();
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
  }

  protected void setPage(WikiPage testPage) {
    this.page = testPage;
    testResponse.rootPath = testPage.getName();
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
    super.allTestingComplete(totalTimeMeasurement);
    setTotalRunTimeOnReport(totalTimeMeasurement);
    writeResults();
  }

  protected void setTotalRunTimeOnReport(TimeMeasurement totalTimeMeasurement) {
    testResponse.setTotalRunTimeInMillis(totalTimeMeasurement);
  }

  protected void writeResults() throws IOException {
    writeResults(writerFactory.getWriter(context, getPageForHistory(), finalSummary, currentTestStartTime));
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
