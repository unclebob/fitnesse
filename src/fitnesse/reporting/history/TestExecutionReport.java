package fitnesse.reporting.history;

import fitnesse.FitNesseVersion;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fitnesse.testsystems.TestSummary;

import fitnesse.util.XmlUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class TestExecutionReport extends ExecutionReport {
  private List<TestResult> results = new ArrayList<>();

  public TestExecutionReport(FitNesseVersion version, String rootPath) {
    super(version, rootPath);
  }

  public TestExecutionReport(InputStream input) throws IOException, SAXException, InvalidReportException {
    Document xmlDoc = XmlUtil.newDocument(input);
    unpackXml(xmlDoc);
  }

  public TestExecutionReport(File file) throws IOException, SAXException, InvalidReportException {
    Document xmlDoc = XmlUtil.newDocument(file);
    unpackXml(xmlDoc);
  }

  public TestExecutionReport(Document xmlDocument) throws InvalidReportException {
    unpackXml(xmlDocument);
  }

  @Override
  protected void unpackResults(Element testResults) {
    NodeList xmlResults = testResults.getElementsByTagName("result");
    for (int resultIndex = 0; resultIndex < xmlResults.getLength(); resultIndex++) {
      unpackResult(xmlResults, resultIndex);
    }
  }

  private void unpackResult(NodeList xmlResults, int resultIndex) {
    Element xmlResult = (Element) xmlResults.item(resultIndex);
    TestResult result = new TestResult();
    result.content = XmlUtil.getTextValue(xmlResult, "content");
    result.right = XmlUtil.getTextValue(xmlResult, "right");
    result.wrong = XmlUtil.getTextValue(xmlResult, "wrong");
    result.ignores = XmlUtil.getTextValue(xmlResult, "ignores");
    result.exceptions = XmlUtil.getTextValue(xmlResult, "exceptions");
    result.relativePageName = XmlUtil.getTextValue(xmlResult, "relativePageName");
    result.tags = XmlUtil.getTextValue(xmlResult, "tags");
    result.dateString = XmlUtil.getTextValue(xmlResult, "date");
    result.runTimeInMillis = XmlUtil.getTextValue(xmlResult, "runTimeInMillis");

    Element xmlInstructions = XmlUtil.getElementByTagName(xmlResult, "instructions");
    if (xmlInstructions != null) {
      unpackInstructions(result, xmlInstructions);
    }
    addResult(result);
  }

  private void unpackInstructions(TestResult result, Element xmlInstructions) {
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

  private void unpackExpectations(Element instructionElement, InstructionResult instructionResult) {
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

  public List<TestResult> getResults() {
    return new ArrayList<>(results);
  }

  public void addResult(TestResult currentResult) {
    results.add(currentResult);
  }

  public void toXml(Writer writer, VelocityEngine velocityEngine) {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("response", this);
    Template template = velocityEngine.getTemplate("testResults.vm");
    template.merge(velocityContext, writer);
  }

  public TestSummary getAssertionCounts() {
    TestSummary assertionCounts = new TestSummary();
    for (TestResult result : results) {
      assertionCounts.add(result.getTestSummary());
    }
    return assertionCounts;
  }

  public String getContentsOfReport(int reportNumber) {
    return results.get(reportNumber).getContent();
  }

  public static class TestResult {
    public String right;
    public String wrong;
    public String ignores;
    public String exceptions;
    public String content;
    public String relativePageName;
    public List<InstructionResult> instructions = new ArrayList<>();
    public String tags;
    public String dateString;
    public long startTime;
    public String runTimeInMillis;

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

    public String getDateString() {
      return dateString;
    }

    public String getRunTimeInMillis() {
      return runTimeInMillis;
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

    public TestSummary getTestSummary() {
      try {
        return new TestSummary(
                Integer.parseInt(right),
                Integer.parseInt(wrong),
                Integer.parseInt(ignores),
                Integer.parseInt(exceptions)
        );
      } catch (NumberFormatException e) {
        return new TestSummary();
      }
    }

    public void addInstruction(InstructionResult instructionResult) {
      instructions.add(instructionResult);
    }
  }

  public static class InstructionResult {
    public String instruction;
    public String slimResult;
    private List<Expectation> expectations = new ArrayList<>();

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
