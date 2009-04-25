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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class XmlFormatter extends BaseFormatter {
  protected TestResponse testResponse = new TestResponse();
  private TestResponse.TestResult currentResult;
  private StringBuilder outputBuffer;
  private TestSystem testSystem;
  private FileWriter fileWriter;
  private static long testTime;
  private TestSummary finalSummary = new TestSummary();
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
    currentResult = new TestResponse.TestResult();
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
    fileWriter.close();
  }

  private void makeFileWriter() throws Exception {
    File resultPath = new File(String.format("%s/%s/files/testResults/%s/%s",
      context.rootPath,
      context.rootDirectoryName,
      page.getPageCrawler().getFullPath(page).toString(),
      makeResultFileName(getFinalSummary())));
    File resultDirectory = new File(resultPath.getParent());
    resultDirectory.mkdirs();
    File resultFile = new File(resultDirectory, resultPath.getName());
    fileWriter = new FileWriter(resultFile);
  }

  protected TestSummary getFinalSummary() {
    return finalSummary;
  }

  public static String makeResultFileName(TestSummary summary) {
    SimpleDateFormat format = new SimpleDateFormat(TEST_RESULT_FILE_DATE_PATTERN);
    String datePart = format.format(new Date(getTime()));
    return String.format("%s_%d_%d_%d_%d.xml", datePart, summary.right, summary.wrong, summary.ignores, summary.exceptions);
  }

  private Writer getWriter() {
    Writer writer = new Writer() {
      public void write(char[] cbuf, int off, int len) {
        String fragment = new String(cbuf, off, len);
        try {
          writeData(fragment.getBytes());
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
    currentResult.right = Integer.toString(testSummary.right);
    currentResult.wrong = Integer.toString(testSummary.wrong);
    currentResult.ignores = Integer.toString(testSummary.ignores);
    currentResult.exceptions = Integer.toString(testSummary.exceptions);

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
    private TestResponse.TestResult testResult;
    private List<Object> instructions;
    private Map<String, Object> results;
    private List<SlimTable.Expectation> expectations;
    private List<SlimTable> slimTables;

    public SlimTestXmlFormatter(TestResponse.TestResult testResult, SlimTestSystem slimSystem) {
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
        testResult.tables = new ArrayList<TestResponse.Table>();
        for (SlimTable slimTable : slimTables) {
          addTable(slimTable);
        }
      }
    }

    private void addTable(SlimTable slimTable) {
      TestResponse.Table resultTable = new TestResponse.Table(slimTable.getTableName());
      testResult.tables.add(resultTable);
      addRowsToTable(slimTable, resultTable);
      addChildTables(slimTable);
    }

    private void addChildTables(SlimTable slimTable) {
      for (SlimTable child : slimTable.getChildren()) {
        addTable(child);
      }
    }

    private void addRowsToTable(SlimTable slimTable, TestResponse.Table resultTable) {
      Table table = slimTable.getTable();
      int rows = table.getRowCount();
      for (int row = 0; row < rows; row++) {
        addRowToTable(resultTable, table, row);
      }
    }

    private void addRowToTable(TestResponse.Table resultTable, Table table, int row) {
      TestResponse.Row resultRow = new TestResponse.Row();
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

    private void addColorizedScenarioReference(TestResponse.Row resultRow, String contents) {
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
      TestResponse.InstructionResult instructionResult = new TestResponse.InstructionResult();
      testResult.instructions.add(instructionResult);

      List<Object> instructionList = (List<Object>) instruction;
      String id = (String) (instructionList.get(0));
      Object result = results.get(id);

      instructionResult.instruction = instruction.toString();
      instructionResult.slimResult = result.toString();
      for (SlimTable.Expectation expectation : expectations) {
        if (expectation.getInstructionTag().equals(id)) {
          try {
            TestResponse.Expectation expectationResult = new TestResponse.Expectation();
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

  public static class TestResponse {
    private String version;
    private String rootPath;
    private List<TestResult> results = new ArrayList<TestResult>();
    protected Counts finalCounts;

    public String getVersion() {
      return version;
    }

    public String getRootPath() {
      return rootPath;
    }

    public List<TestResult> getResults() {
      return results;
    }

    public Counts getFinalCounts() {
      return finalCounts;
    }

    public static class TestResult {
      private String right;
      private String wrong;
      private String ignores;
      private String exceptions;
      private String content;
      private String relativePageName;
      private List<InstructionResult> instructions = new ArrayList<InstructionResult>();
      private String tags;
      private ArrayList<Table> tables;

      public String getRight() {
        return right;
      }

      public String getWrong() {
        return wrong;
      }

      public String getIgnores() {
        return ignores;
      }

      public String getExceptions() {
        return exceptions;
      }

      public String getContent() {
        return content;
      }

      public String getRelativePageName() {
        return relativePageName;
      }

      public List<InstructionResult> getInstructions() {
        return instructions;
      }

      public String getTags() {
        return tags;
      }

      public void setTags(String tags) {
        this.tags = tags;
      }

      public ArrayList<Table> getTables() {
        return tables;
      }
    }

    public static class InstructionResult {
      private String instruction;
      private String slimResult;
      private List<Expectation> expectations = new ArrayList<Expectation>();

      public void addExpectation(Expectation expectation) {
        expectations.add(expectation);
      }

      public String getInstruction() {
        return instruction;
      }

      public String getSlimResult() {
        return slimResult;
      }

      public List<Expectation> getExpectations() {
        return expectations;
      }
    }

    public static class Expectation {
      private String instructionId;
      private String col;
      private String row;
      private String type;
      private String actual;
      private String expected;
      private String evaluationMessage;
      private String status;

      public String getInstructionId() {
        return instructionId;
      }

      public String getCol() {
        return col;
      }

      public String getRow() {
        return row;
      }

      public String getType() {
        return type;
      }

      public String getActual() {
        return actual;
      }

      public String getExpected() {
        return expected;
      }

      public String getEvaluationMessage() {
        return evaluationMessage;
      }

      public String getStatus() {
        return status;
      }
    }

    public static class Counts {
      protected int right;
      protected int wrong;
      protected int ignores;
      protected int exceptions;

      public int getRight() {
        return right;
      }

      public int getWrong() {
        return wrong;
      }

      public int getIgnores() {
        return ignores;
      }

      public int getExceptions() {
        return exceptions;
      }
    }


    public static class Table extends ArrayList<Row>{
      private static final long serialVersionUID = 1L;
      private String name;

      public Table(String tableName) {
        this.name = tableName;
      }

      public String getName() {
        return name;
      }
    }

    public static class Row extends ArrayList<String> {
      private static final long serialVersionUID = 1L;
    }
  }
}
