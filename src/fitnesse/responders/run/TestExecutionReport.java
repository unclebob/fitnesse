package fitnesse.responders.run;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.XmlUtil;
import static util.XmlUtil.getElementByTagName;

import java.io.InputStream;
import java.io.Writer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class TestExecutionReport {
  public String version;
  public String rootPath;
  public List<TestResult> results = new ArrayList<TestResult>();
  public TestSummary finalCounts;
  private Date date;
  private Document xmlDoc;

  public TestExecutionReport(File input) throws Exception {
    xmlDoc = XmlUtil.newDocument(input);
    unpackXml();
  }

  public TestExecutionReport(InputStream input) throws Exception {
    xmlDoc = XmlUtil.newDocument(input);
    unpackXml();
  }

  private void unpackXml() throws Exception {
    Element testResults = xmlDoc.getDocumentElement();
    version = XmlUtil.getTextValue(testResults, "FitNesseVersion");
    rootPath = XmlUtil.getTextValue(testResults, "rootPath");
    Element counts = getElementByTagName(testResults, "finalCounts");
    if (counts != null) {
      finalCounts = new TestSummary(
        Integer.parseInt(XmlUtil.getTextValue(counts, "right")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "wrong")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "ignores")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "exceptions"))
      );
    }
    NodeList xmlResults = testResults.getElementsByTagName("result");
    for (int item = 0; item < xmlResults.getLength(); item++) {
      Element xmlResult = (Element) xmlResults.item(item);
      TestResult result = new TestResult();
      results.add(result);
      result.content = XmlUtil.getTextValue(xmlResult, "content");
      result.right = XmlUtil.getTextValue(xmlResult, "right");
      result.wrong = XmlUtil.getTextValue(xmlResult, "wrong");
      result.ignores = XmlUtil.getTextValue(xmlResult, "ignores");
      result.exceptions = XmlUtil.getTextValue(xmlResult, "exceptions");
      result.relativePageName = XmlUtil.getTextValue(xmlResult, "relativePageName");
      result.tags = XmlUtil.getTextValue(xmlResult, "tags");
    }
  }

  public TestExecutionReport() {
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
    public ArrayList<Table> tables;

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
