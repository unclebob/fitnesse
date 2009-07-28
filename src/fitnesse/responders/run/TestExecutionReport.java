package fitnesse.responders.run;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.XmlUtil;
import static util.XmlUtil.getElementByTagName;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestExecutionReport {
  public String version;
  public String rootPath;
  public List<TestResult> results = new ArrayList<TestResult>();
  public TestSummary finalCounts;
  private Date date;
  private Document xmlDoc;

  public TestExecutionReport() {
  }

  public TestExecutionReport(InputStream input) throws Exception {
    xmlDoc = XmlUtil.newDocument(input);
    unpackXml();
  }

  public TestExecutionReport read(File file) throws Exception {
    xmlDoc = XmlUtil.newDocument(file);
    unpackXml();
    return this;
  }

  private void unpackXml() throws Exception {
    Element testResults = xmlDoc.getDocumentElement();
    version = XmlUtil.getTextValue(testResults, "FitNesseVersion");
    rootPath = XmlUtil.getTextValue(testResults, "rootPath");
    unpackFinalCounts(testResults);
    unpackResults(testResults);
  }

  private void unpackFinalCounts(Element testResults) throws Exception {
    Element counts = getElementByTagName(testResults, "finalCounts");
    if (counts != null) {
      finalCounts = new TestSummary(
        Integer.parseInt(XmlUtil.getTextValue(counts, "right")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "wrong")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "ignores")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "exceptions"))
      );
    }
  }

  private void unpackResults(Element testResults) throws Exception {
    NodeList xmlResults = testResults.getElementsByTagName("result");
    for (int resultIndex = 0; resultIndex < xmlResults.getLength(); resultIndex++) {
      unpackResult(xmlResults, resultIndex);
    }
  }

  private void unpackResult(NodeList xmlResults, int resultIndex) throws Exception {
    Element xmlResult = (Element) xmlResults.item(resultIndex);
    TestResult result = new TestResult();
    results.add(result);
    result.content = XmlUtil.getTextValue(xmlResult, "content");
    result.right = XmlUtil.getTextValue(xmlResult, "right");
    result.wrong = XmlUtil.getTextValue(xmlResult, "wrong");
    result.ignores = XmlUtil.getTextValue(xmlResult, "ignores");
    result.exceptions = XmlUtil.getTextValue(xmlResult, "exceptions");
    result.relativePageName = XmlUtil.getTextValue(xmlResult, "relativePageName");
    result.tags = XmlUtil.getTextValue(xmlResult, "tags");

    unpackTables(xmlResult, result);
    Element xmlInstructions = XmlUtil.getElementByTagName(xmlResult, "instructions");
    if (xmlInstructions != null) {
      unpackInstructions(result, xmlInstructions);
    }
  }

  private void unpackInstructions(TestResult result, Element xmlInstructions) throws Exception {
    NodeList xmlInstructionResults = xmlInstructions.getElementsByTagName("instructionResult");
    for (int instructionIndex = 0; instructionIndex < xmlInstructionResults.getLength(); instructionIndex++) {
      Element instructionElement = (Element) xmlInstructionResults.item(instructionIndex);
      String instruction = XmlUtil.getTextValue(instructionElement, "instruction");
      String slimResult = XmlUtil.getTextValue(instructionElement, "slimResult");
      InstructionResult instructionResult = new InstructionResult();
      instructionResult.instruction = instruction;
      instructionResult.slimResult = slimResult;
      result.instructions.add(instructionResult);
      unpackExpectations(instructionElement, instructionResult);
    }
  }

  private void unpackExpectations(Element instructionElement, InstructionResult instructionResult) throws Exception {
    NodeList xmlExpectations = instructionElement.getElementsByTagName("expectation");
    for (int expectationIndex = 0; expectationIndex < xmlExpectations.getLength(); expectationIndex++) {
      Element expectationElement = (Element) xmlExpectations.item(expectationIndex);
      Expectation expectation = new Expectation();
      instructionResult.addExpectation(expectation);
      expectation.status = XmlUtil.getTextValue(expectationElement, "status");
      expectation.instructionId = XmlUtil.getTextValue(expectationElement, "instructionId");
      expectation.col = XmlUtil.getTextValue(expectationElement, "col");
      expectation.row = XmlUtil.getTextValue(expectationElement, "row");
      expectation.type = XmlUtil.getTextValue(expectationElement, "type");
      expectation.actual = XmlUtil.getTextValue(expectationElement, "actual");
      expectation.expected = XmlUtil.getTextValue(expectationElement, "expected");
      expectation.evaluationMessage = XmlUtil.getTextValue(expectationElement, "evaluationMessage");
    }
  }

  private void unpackTables(Element xmlResult, TestResult result) throws Exception {
    NodeList tables = xmlResult.getElementsByTagName("tables");
    for (int tableIndex = 0; tableIndex < tables.getLength(); tableIndex++) {
      Element xmlTable = (Element) tables.item(tableIndex);
      String tableName = XmlUtil.getTextValue(xmlTable, "name");
      Table table = new Table(tableName);
      result.tables.add(table);
      unpackTable(xmlTable, table);
    }
  }

  private void unpackTable(Element xmlTable, Table table) throws Exception {
    NodeList xmlRows = xmlTable.getElementsByTagName("row");
    for (int rowIndex = 0; rowIndex < xmlRows.getLength(); rowIndex++) {
      Element xmlRow = (Element) xmlRows.item(rowIndex);
      unpackRow(table, xmlRow);
    }
  }

  private void unpackRow(Table table, Element xmlRow) throws Exception {
    Row row = new Row();
    table.add(row);
    NodeList xmlCols = xmlRow.getElementsByTagName("col");
    for (int colIndex = 0; colIndex < xmlCols.getLength(); colIndex++) {
      Element xmlCol = (Element) xmlCols.item(colIndex);
      String colText = XmlUtil.getElementText(xmlCol);
      row.add(colText);
    }
  }

  public String getVersion() {
    return version;
  }

  public String getRootPath() {
    return rootPath;
  }

  public List<TestResult> getResults() {
    return results;
  }

  public TestSummary getFinalCounts() {
    return finalCounts;
  }

  public void toXml(Writer writer, VelocityEngine velocityEngine) throws Exception {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", this);
    Template template = velocityEngine.getTemplate("testResults.vm");
    template.merge(velocityContext, writer);
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public TestSummary getAssertionCounts() {
    TestSummary assertionCounts = new TestSummary();
    for (TestResult result : results) {
      assertionCounts.add(result.getTestSummary());
    }
    return assertionCounts;
  }

  public static String summaryClass(TestSummary testSummary) {
    if (testSummary.right > 0 && testSummary.wrong == 0 && testSummary.exceptions == 0)
      return "pass";
    else if (testSummary.wrong > 0)
      return "fail";
    else if (testSummary.exceptions > 0)
      return "error";
    else
      return "ignore";
  }

  public static class TestResult {
    public String right;
    public String wrong;
    public String ignores;
    public String exceptions;
    public String content;
    public String relativePageName;
    public List<InstructionResult> instructions = new ArrayList<InstructionResult>();
    public String tags;
    public List<Table> tables = new ArrayList<Table>();
    public long startTime;

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

    public List<Table> getTables() {
      return tables;
    }

    public TestSummary getTestSummary() {
      return new TestSummary(
        Integer.parseInt(right),
        Integer.parseInt(wrong),
        Integer.parseInt(ignores),
        Integer.parseInt(exceptions)
      );
    }
  }

  public static class InstructionResult {
    public String instruction;
    public String slimResult;
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
    public String instructionId;
    public String col;
    public String row;
    public String type;
    public String actual;
    public String expected;
    public String evaluationMessage;
    public String status;

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

  public static class Table extends ArrayList<Row> {
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
