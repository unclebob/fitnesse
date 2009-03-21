// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fitnesse.FitNesseVersion;
import fitnesse.slimTables.SlimTable;
import fitnesse.responders.run.slimResponder.SlimTestSystem;
import util.XmlWriter;
import fitnesse.html.HtmlUtil;
import util.XmlUtil;
import fitnesse.wiki.WikiPage;

public abstract class XmlFormatter extends BaseFormatter {

  final private Document testResultsDocument = XmlUtil.newDocument();
  private Element testResultsElement;
  private StringBuffer outputBuffer;
  private TestSystem testSystem;

  public XmlFormatter(final WikiPage page) throws Exception {
    super(page);
  }

  public void announceStartNewTest(WikiPage test) throws Exception {
    appendHtmlToBuffer(HtmlUtil.getHtmlOfInheritedPage("PageHeader", getPage()));
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
  
    Element resultElement = testResultsDocument.createElement("result");
    testResultsElement.appendChild(resultElement);
    addCountsToResult(testSummary, resultElement);
  
    XmlUtil.addCdataNode(testResultsDocument, resultElement, "content", outputBuffer.toString());
    outputBuffer = null;
  
    XmlUtil.addTextNode(testResultsDocument, resultElement, "relativePageName", relativeTestName);
    if (testSystem instanceof SlimTestSystem) {
      SlimTestSystem slimSystem = (SlimTestSystem) testSystem;
      new InstructionXmlFormatter(resultElement, slimSystem).invoke();
    }
  }

  public void setExecutionLogAndTrackingId(String stopResponderId,
      CompositeExecutionLog log) throws Exception {
  }
  
  @Override
  public void writeHead(String pageType) throws Exception {
    testResultsElement = testResultsDocument.createElement("testResults");
    testResultsDocument.appendChild(testResultsElement);
    XmlUtil.addTextNode(testResultsDocument, testResultsElement, "FitNesseVersion", new FitNesseVersion().toString());
    XmlUtil.addTextNode(testResultsDocument, testResultsElement, "rootPath", getPage().getName());
  }
  
  public void allTestingComplete() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    XmlWriter writer = new XmlWriter(os);
    writer.write(testResultsDocument);
    writer.close();
    writeData(os.toByteArray());
    close();
  }
  
  protected abstract void writeData(byte[] byteArray) throws Exception;
  
  protected void close() throws Exception {
    
  }
  
  private void addCountsToResult(TestSummary testSummary, Element resultElement) {
    Element counts = testResultsDocument.createElement("counts");
    resultElement.appendChild(counts);
    XmlUtil.addTextNode(testResultsDocument, counts, "right", Integer.toString(testSummary.right));
    XmlUtil.addTextNode(testResultsDocument, counts, "wrong", Integer.toString(testSummary.wrong));
    XmlUtil.addTextNode(testResultsDocument, counts, "ignores", Integer.toString(testSummary.ignores));
    XmlUtil.addTextNode(testResultsDocument, counts, "exceptions", Integer.toString(testSummary.exceptions));
  }
  
  private void appendHtmlToBuffer(String output) {
    if (outputBuffer == null) {
      outputBuffer = new StringBuffer();
    }
    outputBuffer.append(output);
  }

  public Document getDocument() {
    return testResultsDocument;
  }

  public Element getTestResultsElement() {
    return testResultsElement;
  }


  private class InstructionXmlFormatter {
    private Element resultElement;
    private SlimTestSystem slimSystem;
    private List<Object> instructions;
    private Map<String, Object> results;
    private List<SlimTable.Expectation> expectations;

    public InstructionXmlFormatter(Element resultElement, SlimTestSystem slimSystem) {
      this.resultElement = resultElement;
      this.slimSystem = slimSystem;
      instructions = slimSystem.getInstructions();
      results = slimSystem.getInstructionResults();
      expectations = slimSystem.getExpectations();
    }

    public void invoke() {
      Element instructionsElement = testResultsDocument.createElement("instructions");
      resultElement.appendChild(instructionsElement);
      addInstructionResultss(instructionsElement);
    }

    private void addInstructionResultss(Element instructionsElement) {
      for (Object instruction : instructions) {
        addInstructionResult(instructionsElement, instruction);
      }
    }

    private void addInstructionResult(Element instructionsElement, Object instruction) {
      List<Object> instructionList = (List<Object>) instruction;
      String id = (String) (instructionList.get(0));

      Element instructionElement = testResultsDocument.createElement("instructionResult");
      instructionsElement.appendChild(instructionElement);

      addInstruction(instruction, instructionElement);
      addResult(id, instructionElement);
      addExpectationIfPresent(id, instructionElement);
    }

    private void addExpectationIfPresent(String id, Element instructionElement) {
      for (SlimTable.Expectation expectation : expectations) {
        if (expectation.getInstructionTag().equals(id)) {
          addExpectation(instructionElement, expectation);
          break;
        }
      }
    }

    private void addExpectation(Element instructionElement, SlimTable.Expectation expectation) {
      Element expectationElement = testResultsDocument.createElement("expectation");
      instructionElement.appendChild(expectationElement);
      XmlUtil.addTextNode(testResultsDocument, expectationElement, "instructionId", expectation.getInstructionTag());
      XmlUtil.addTextNode(testResultsDocument, expectationElement, "col", Integer.toString(expectation.getCol()));
      XmlUtil.addTextNode(testResultsDocument, expectationElement, "row", Integer.toString(expectation.getRow()));
      XmlUtil.addTextNode(testResultsDocument, expectationElement, "type", expectation.getClass().getSimpleName());
      XmlUtil.addCdataNode(testResultsDocument, expectationElement, "actual", expectation.getActual());
      XmlUtil.addCdataNode(testResultsDocument, expectationElement, "expected", expectation.getExpected());
      XmlUtil.addCdataNode(testResultsDocument, expectationElement, "evaluationMessage", expectation.getEvaluationMessage());
    }

    private void addResult(String id, Element instructionElement) {
      Object result = results.get(id);
      XmlUtil.addCdataNode(testResultsDocument, instructionElement, "slimResult", result.toString());
    }

    private void addInstruction(Object instruction, Element instructionElement) {
      XmlUtil.addCdataNode(testResultsDocument, instructionElement, "instruction", instruction.toString());
    }
  }



}
