// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.VelocityFactory;
import fitnesse.responders.run.*;
import fitnesse.responders.run.slimResponder.SlimTestSystem;
import fitnesse.slimTables.HtmlTable;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.Table;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import util.TimeMeasurement;

import java.io.Writer;
import java.util.List;
import java.util.Map;

public class XmlFormatter extends BaseFormatter {
  public interface WriterFactory {
    Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws Exception;
  }

  private WriterFactory writerFactory;
  private long currentTestStartTime;
  private StringBuilder outputBuffer;
  private TestSystem testSystem;
  protected TestExecutionReport testResponse = new TestExecutionReport();
  protected TestSummary finalSummary = new TestSummary();

  public XmlFormatter(FitNesseContext context, final WikiPage page, WriterFactory writerFactory) throws Exception {
    super(context, page);
    this.writerFactory = writerFactory;
  }
  
  @Override
  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws Exception {
    currentTestStartTime = timeMeasurement.startedAt();
    appendHtmlToBuffer(getPage().getData().getHeaderPageHtml());
  }
  
  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
    this.testSystem = testSystem;
  }
  
  @Override
  public void testOutputChunk(String output) throws Exception {
    appendHtmlToBuffer(output);
  }
  
  @Override
  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement)
    throws Exception {
    super.testComplete(test, testSummary, timeMeasurement);
    processTestResults(test.getName(), testSummary, timeMeasurement);
  }

  public void processTestResults(final String relativeTestName, TestSummary testSummary, TimeMeasurement timeMeasurement)
    throws Exception {
    finalSummary = new TestSummary(testSummary);
    TestExecutionReport.TestResult currentResult = newTestResult();
    testResponse.results.add(currentResult);
    currentResult.startTime = currentTestStartTime;
    currentResult.content = outputBuffer == null ? null : outputBuffer.toString();
    outputBuffer = null;
    addCountsToResult(currentResult, testSummary);
    currentResult.runTimeInMillis = String.valueOf(timeMeasurement.elapsed());
    currentResult.relativePageName = relativeTestName;
    currentResult.tags = page.getData().getAttribute(PageData.PropertySUITES);

    if (testSystem instanceof SlimTestSystem) {
      SlimTestSystem slimSystem = (SlimTestSystem) testSystem;
      new SlimTestXmlFormatter(currentResult, slimSystem).invoke();
    }
  }

  protected TestExecutionReport.TestResult newTestResult() {
    return new TestExecutionReport.TestResult();
  }
  
  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId,
                                           CompositeExecutionLog log) throws Exception {
  }
  
  @Override
  public void writeHead(String pageType) throws Exception {
    writeHead(getPage());
  }

  protected void writeHead(WikiPage testPage) throws Exception {
    testResponse.version = new FitNesseVersion().toString();
    testResponse.rootPath = testPage.getName();
  }

  @Override
  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws Exception {
    super.allTestingComplete(totalTimeMeasurement);
    setTotalRunTimeOnReport(totalTimeMeasurement);
    writeResults();
  }

  protected void setTotalRunTimeOnReport(TimeMeasurement totalTimeMeasurement) {
    testResponse.setTotalRunTimeInMillis(totalTimeMeasurement);
  }
  
  protected void writeResults() throws Exception {
    writeResults(writerFactory.getWriter(context, getPageForHistory(), finalSummary, currentTestStartTime));
  }

  protected WikiPage getPageForHistory() {
    return page;
  }

  @Override
  public int getErrorCount() {
    return finalSummary.wrong + finalSummary.exceptions;
  }

  protected void writeResults(Writer writer) throws Exception {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", testResponse);
    Template template = VelocityFactory.getVelocityEngine().getTemplate("testResults.vm");
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

  private static class SlimTestXmlFormatter {
    private TestExecutionReport.TestResult testResult;
    private List<Object> instructions;
    private Map<String, Object> results;
    private List<SlimTable.Expectation> expectations;
    private List<SlimTable> slimTables;

    public SlimTestXmlFormatter(TestExecutionReport.TestResult testResult, SlimTestSystem slimSystem) {
      this.testResult = testResult;
      instructions = slimSystem.getInstructions();
      results = slimSystem.getInstructionResults();
      expectations = slimSystem.getExpectations();
      slimTables = slimSystem.getTestTables();
    }

    public void invoke() {
      addTables();
      addInstructionResults();
    }

    private void addTables() {
      if (slimTables.size() > 0) {
        for (SlimTable slimTable : slimTables) {
          addTable(slimTable);
        }
      }
    }

    private void addTable(SlimTable slimTable) {
      TestExecutionReport.Table resultTable = new TestExecutionReport.Table(slimTable.getTableName());
      testResult.tables.add(resultTable);
      addRowsToTable(slimTable, resultTable);
      addChildTables(slimTable);
    }

    private void addChildTables(SlimTable slimTable) {
      for (SlimTable child : slimTable.getChildren()) {
        addTable(child);
      }
    }

    private void addRowsToTable(SlimTable slimTable, TestExecutionReport.Table resultTable) {
      Table table = slimTable.getTable();
      int rows = table.getRowCount();
      for (int row = 0; row < rows; row++) {
        addRowToTable(resultTable, table, row);
      }
    }

    private void addRowToTable(TestExecutionReport.Table resultTable, Table table, int row) {
      TestExecutionReport.Row resultRow = new TestExecutionReport.Row();
      resultTable.add(resultRow);
      int cols = table.getColumnCountInRow(row);
      for (int col = 0; col < cols; col++) {
        String contents = table.getCellContents(col, row);
        if (isScenarioHtml(contents)) {
          addColorizedScenarioReference(resultRow, contents);
        } else {
          String colorizedContents = HtmlTable.colorize(contents);
          resultRow.add(colorizedContents);
        }
      }
    }

    private void addColorizedScenarioReference(TestExecutionReport.Row resultRow, String contents) {
      String status = getTestStatus(contents);
      String tableName = getTableName(contents);
      resultRow.add(String.format("%s(scenario:%s)", status, tableName));
    }

    private String getTableName(String contents) {
      return getStringBetween(contents, "table_name=\"", "\"");
    }

    private static String getTestStatus(String contents) {
      return getStringBetween(contents, "<span id=\"test_status\" class=", ">Scenario</span>");
    }

    private static String getStringBetween(String contents, String prefix, String suffix) {
      int start = contents.indexOf(prefix) + prefix.length();
      int end = contents.indexOf(suffix, start);
      return contents.substring(start, end);
    }

    private boolean isScenarioHtml(String contents) {
      return contents.startsWith("<div class=\"collapse_rim\">");
    }

    private void addInstructionResults() {
      for (Object instruction : instructions) {
        addInstructionResult(instruction);
      }
    }

    @SuppressWarnings("unchecked")
    private void addInstructionResult(Object instruction) {
      TestExecutionReport.InstructionResult instructionResult = new TestExecutionReport.InstructionResult();
      testResult.instructions.add(instructionResult);

      List<Object> instructionList = (List<Object>) instruction;
      String id = (String) (instructionList.get(0));
      Object result = results.get(id);

      instructionResult.instruction = instruction.toString();
      instructionResult.slimResult = (result != null) ? result.toString() : "";
      for (SlimTable.Expectation expectation : expectations) {
        if (expectation.getInstructionTag().equals(id)) {
          try {
            TestExecutionReport.Expectation expectationResult = new TestExecutionReport.Expectation();
            instructionResult.addExpectation(expectationResult);
            expectationResult.instructionId = expectation.getInstructionTag();
            expectationResult.col = Integer.toString(expectation.getCol());
            expectationResult.row = Integer.toString(expectation.getRow());
            expectationResult.type = expectation.getClass().getSimpleName();
            expectationResult.actual = expectation.getActual();
            expectationResult.expected = expectation.getExpected();
            String message = expectation.getEvaluationMessage();
            expectationResult.evaluationMessage = message;
            expectationResult.status = expectationStatus(message);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }
    }

    private String expectationStatus(String message) {
      String status = "TILT";
      if (message.matches(".*pass(.*)"))
        status = "right";
      else if (message.matches(".*fail(.*)"))
        status = "wrong";
      else if (message.matches(".*__EXCEPTION__:<"))
        status = "exception";
      else
        status = "ignored";
      return status;
    }
  }

}
