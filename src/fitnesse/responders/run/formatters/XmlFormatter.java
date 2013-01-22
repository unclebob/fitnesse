// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.TestPage;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.testsystems.slim.Table;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlFormatter extends BaseFormatter {
  public interface WriterFactory {
    Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException;
  }

  private WriterFactory writerFactory;
  private long currentTestStartTime;
  private StringBuilder outputBuffer;
  private TestSystem testSystem;
  protected TestExecutionReport testResponse = new TestExecutionReport();
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
    this.testSystem = testSystem;
  }

  @Override
  public void testOutputChunk(String output) {
    appendHtmlToBuffer(output);
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

    if (testSystem instanceof SlimTestSystem) {
      SlimTestSystem slimSystem = (SlimTestSystem) testSystem;
      new SlimTestXmlFormatter(currentResult, slimSystem).invoke();
    }
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

  private static class SlimTestXmlFormatter {
    private TestExecutionReport.TestResult testResult;
    private List<Assertion> assertions;
    private Map<String, Object> results;
    private List<SlimTable> slimTables;

    public SlimTestXmlFormatter(TestExecutionReport.TestResult testResult, SlimTestSystem slimSystem) {
      this.testResult = testResult;
      assertions = slimSystem.getAssertions();
      results = slimSystem.getInstructionResults();
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
        // TODO: -AJM- content is extracted from the HTML output. Yuck!
        String contents = table.getCellContents(col, row);
        if (isScenarioHtml(contents)) {
          addColorizedScenarioReference(resultRow, contents);
        } else {
          String colorizedContents = colorize(contents);
          resultRow.add(colorizedContents);
        }
      }
    }

    private void addColorizedScenarioReference(TestExecutionReport.Row resultRow, String contents) {
      String status = getTestStatus(contents);
      String tableName = getTableName(contents);
      resultRow.add(String.format("%s(scenario:%s)", status, tableName));
    }

    private static Pattern coloredCellPattern = Pattern.compile("<span class=\"(\\w*)\">(.*)(</span>)");

    // This terrible algorithm is an example of either my hatred, or my ignorance, of regular expressions.
    public static String colorize(String content) {
      while (true) {
        int firstMatchEnd = content.indexOf("</span>");
        if (firstMatchEnd != -1) {
          firstMatchEnd += "</span>".length();
          Matcher matcher = coloredCellPattern.matcher(content);
          matcher.region(0, firstMatchEnd);
          if (matcher.find()) {
            String color = matcher.group(1);
            String coloredString = matcher.group(2);
            content = content.replace(matcher.group(), String.format("%s(%s)", color, coloredString));
          } else
            break;
        } else {
          break;
        }
      }
      return content;
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
      return contents.startsWith("<div class=\"collapsible\">");
    }

    private void addInstructionResults() {
      for (Assertion assertion : assertions) {
        addInstructionResult(assertion);
      }
    }

    @SuppressWarnings("unchecked")
    private void addInstructionResult(Assertion assertion) {
      Instruction instruction = assertion.getInstruction();
      Expectation expectation = assertion.getExpectation();
      if (instruction != Instruction.NOOP_INSTRUCTION) {
        TestExecutionReport.InstructionResult instructionResult = new TestExecutionReport.InstructionResult();
        testResult.instructions.add(instructionResult);

        String id = instruction.getId();
        Object result = results.get(id);

        instructionResult.instruction = instruction.toString();
        instructionResult.slimResult = (result != null) ? result.toString() : "";
        if (expectation instanceof SlimTable.RowExpectation) {
          SlimTable.RowExpectation rowExpectation = (SlimTable.RowExpectation) expectation;
          try {
            TestExecutionReport.Expectation expectationResult = new TestExecutionReport.Expectation();
            instructionResult.addExpectation(expectationResult);
            expectationResult.instructionId = id;
            expectationResult.col = Integer.toString(rowExpectation.getCol());
            expectationResult.row = Integer.toString(rowExpectation.getRow());
            expectationResult.type = rowExpectation.getClass().getSimpleName();
            expectationResult.actual = rowExpectation.getActual();
            expectationResult.expected = rowExpectation.getExpected();
            String message = rowExpectation.getEvaluationMessage();
            expectationResult.evaluationMessage = message;
            expectationResult.status = expectationStatus(message);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }
    }

    // TODO: -AJM- Get rid of this bad example of coding
    private String expectationStatus(String message) {
      String status;
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
