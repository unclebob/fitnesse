// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import fitnesse.responders.run.slimResponder.SlimTestSystem;
import fitnesse.slimTables.HtmlTable;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.Table;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class XmlFormatter extends BaseFormatter {
  protected TestExecutionReport testResponse = new TestExecutionReport();
  private TestExecutionReport.TestResult currentResult;
  private StringBuilder outputBuffer;
  private TestSystem testSystem;
  private FileWriter fileWriter;
  private static long testTime;
  protected TestSummary finalSummary = new TestSummary();
  public static final String TEST_RESULT_FILE_DATE_PATTERN = "yyyyMMddHHmmss";

  public XmlFormatter(FitNesseContext context, final WikiPage page) throws Exception {
    super(context, page);
  }

  public void announceStartNewTest(WikiPage test) throws Exception {
    appendHtmlToBuffer(getPage().getData().getHeaderPageHtml());
  }

  public void announceStartTestSystem(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
    this.testSystem = testSystem;
  }

  public void processTestOutput(String output) throws Exception {
    appendHtmlToBuffer(output);
  }

  public void processTestResults(WikiPage test, TestSummary testSummary)
    throws Exception {
    processTestResults(test.getName(), testSummary);
  }

  public void processTestResults(final String relativeTestName, TestSummary testSummary)
    throws Exception {
    finalSummary = new TestSummary(testSummary);
    currentResult = new TestExecutionReport.TestResult();
    testResponse.results.add(currentResult);
    currentResult.content = outputBuffer.toString();
    outputBuffer = null;
    addCountsToResult(testSummary);
    currentResult.relativePageName = relativeTestName;
    currentResult.tags = page.getData().getAttribute(PageData.PropertySUITES);
    
    if (testSystem instanceof SlimTestSystem) {
      SlimTestSystem slimSystem = (SlimTestSystem) testSystem;
      new SlimTestXmlFormatter(currentResult, slimSystem).invoke();
    }
  }

  public void setExecutionLogAndTrackingId(String stopResponderId,
                                           CompositeExecutionLog log) throws Exception {
  }

  public void writeHead(String pageType) throws Exception {
    testResponse.version = new FitNesseVersion().toString();
    testResponse.rootPath = getPage().getName();
  }

  public void allTestingComplete() throws Exception {
    try {
      writeResults();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      close();
    }
  }

  private void writeResults() throws Exception {
    makeFileWriter();
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", testResponse);
    Template template = context.getVelocityEngine().getTemplate("testResults.vm");
    template.merge(velocityContext, getWriter());
    if (fileWriter != null)
      fileWriter.close();
  }

  private void makeFileWriter() throws Exception {
    if (context.shouldCollectHistory) {
      File resultPath = new File(String.format("%s/%s/%s",
        context.getTestHistoryDirectory(),
        page.getPageCrawler().getFullPath(page).toString(),
        makeResultFileName(getFinalSummary())));
      File resultDirectory = new File(resultPath.getParent());
      resultDirectory.mkdirs();
      File resultFile = new File(resultDirectory, resultPath.getName());
      fileWriter = new FileWriter(resultFile);
    } else {
      fileWriter = null;
    }
  }

  protected TestSummary getFinalSummary() {
    return finalSummary;
  }

  public static String makeResultFileName(TestSummary summary) {
    SimpleDateFormat format = new SimpleDateFormat(TEST_RESULT_FILE_DATE_PATTERN);
    String datePart = format.format(new Date(getTime()));
    return String.format("%s_%d_%d_%d_%d.xml", datePart, summary.getRight(), summary.getWrong(), summary.getIgnores(), summary.getExceptions());
  }

  private Writer getWriter() {
    Writer writer = new Writer() {
      public void write(char[] cbuf, int off, int len) {
        String fragment = new String(cbuf, off, len);
        try {
          writeData(fragment.getBytes());
          if (fileWriter != null)
            fileWriter.append(fragment);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public void flush() throws IOException {
      }

      public void close() throws IOException {
      }
    };
    return writer;
  }

  protected abstract void writeData(byte[] byteArray) throws Exception;

  protected void close() throws Exception {

  }

  private void addCountsToResult(TestSummary testSummary) {
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

  public static void setTestTime(String time) {
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    try {
      Date date = format.parse(time);
      testTime = date.getTime();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static long getTime() {
    if (testTime != 0)
      return testTime;
    else
      return System.currentTimeMillis();
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
        testResult.tables = new ArrayList<TestExecutionReport.Table>();
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
