// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseVersion;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.html.TagGroup;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.WikiImportProperty;
import fitnesse.responders.run.slimResponder.SlimTestSystem;
import fitnesse.slimTables.SlimTable;
import fitnesse.wiki.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.XmlUtil;
import util.XmlWriter;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestResponder extends ChunkingResponder implements TestSystemListener, SecureResponder {
  protected static final int htmlDepth = 2;
  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected HtmlPage html;
  protected CompositeExecutionLog log;
  protected PageData data;
  private boolean closed = false;
  protected TestSummary assertionCounts = new TestSummary();
  protected TestHtmlFormatter formatter;
  protected TestSystemGroup testSystemGroup;
  protected String classPath;
  protected Document testResultsDocument;
  protected Element testResultsElement;
  private StringBuffer outputBuffer;
  private boolean fastTest = false;
  protected TestSystem testSystem;

  protected void doSending() throws Exception {
    fastTest |= request.hasInput("debug");
    data = page.getData();
    classPath = buildClassPath();
    startHtml();
    sendPreTestNotification();

    testSystemGroup = new TestSystemGroup(context, page, this);
    testSystemGroup.setFastTest(fastTest);
    log = testSystemGroup.getExecutionLog();

    performExecution();

    finishSending();
  }

  private void sendPreTestNotification() throws Exception {
    for (TestEventListener eventListener : eventListeners) {
      eventListener.notifyPreTest(this, data);
    }
  }

  protected void finishSending() throws Exception {
    completeResponse();
  }

  protected void performExecution() throws Exception {
    TestSystem.Descriptor descriptor = TestSystem.getDescriptor(data);
    testSystem = testSystemGroup.startTestSystem(descriptor, classPath);
    if (testSystemGroup.isSuccessfullyStarted()) {
      addToResponse(HtmlUtil.getHtmlOfInheritedPage("PageHeader", page));
      SetupTeardownIncluder.includeInto(data, true);
      if (data.getContent().length() == 0)
        addEmptyContentMessage();
      testSystem.runTestsAndGenerateHtml(data);
      testSystemGroup.bye();
    }
  }

  protected String buildClassPath() throws Exception {
    return new ClassPathBuilder().getClasspath(page);
  }

  protected void startHtml() throws Exception {
    if (response.isXmlFormat()) {
      testResultsDocument = XmlUtil.newDocument();
      testResultsElement = testResultsDocument.createElement("testResults");
      testResultsDocument.appendChild(testResultsElement);
      XmlUtil.addTextNode(testResultsDocument, testResultsElement, "FitNesseVersion", new FitNesseVersion().toString());
      XmlUtil.addTextNode(testResultsDocument, testResultsElement, "rootPath", page.getName());
    } else {
      buildHtml();
      addToResponse(formatter.head());
    }
  }

  protected PageCrawler getPageCrawler() {
    PageCrawler crawler = root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    return crawler;
  }

  public synchronized void exceptionOccurred(Throwable e) {
    //todo remove sout
    System.err.println("TestResponder.exceptionOcurred:" + e.getMessage());
    try {
      completeResponse();
      testSystemGroup.kill();
    }
    catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  protected synchronized void completeResponse() throws Exception {
    if (!closed) {
      closed = true;
      if (response.isXmlFormat()) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(os);
        writer.write(testResultsDocument);
        writer.close();
        response.add(os.toByteArray());
        response.closeChunks();
        response.close();
      } else {
        log.publish();
        addLogAndClose();
      }
    }
  }

  protected final void addLogAndClose() throws Exception {
    addLog();
    close();
  }

  protected void close() throws Exception {
    response.add(HtmlUtil.getHtmlOfInheritedPage("PageFooter", page));
    response.add(formatter.tail());
    response.closeChunks();
    response.addTrailingHeader("Exit-Code", String.valueOf(exitCode()));
    response.closeTrailer();
    response.close();
  }

  protected void addLog() throws Exception {
    response.add(formatter.testSummary(assertionCounts));
    response.add(formatter.executionStatus(log));
  }

  public void addToResponse(String output) throws Exception {
    if (!closed)
      response.add(output);
  }

  public void acceptResultsLast(TestSummary testSummary) throws Exception {
    if (response.isXmlFormat()) {
      addTestResultsToXmlDocument(testSummary, page.getName());
    } else {
      assertionCounts.tally(testSummary);
    }
  }

  protected void addTestResultsToXmlDocument(TestSummary testSummary, String pageName) throws Exception {
    Element resultElement = testResultsDocument.createElement("result");
    testResultsElement.appendChild(resultElement);
    addCountsToResult(testSummary, resultElement);

    XmlUtil.addCdataNode(testResultsDocument, resultElement, "content", outputBuffer.toString());
    outputBuffer = null;

    XmlUtil.addTextNode(testResultsDocument, resultElement, "relativePageName", pageName);
    if (testSystem instanceof SlimTestSystem) {
      SlimTestSystem slimSystem = (SlimTestSystem) testSystem;
      new InstructionXmlFormatter(resultElement, slimSystem).invoke();
    }

  }

  private void addCountsToResult(TestSummary testSummary, Element resultElement) {
    Element counts = testResultsDocument.createElement("counts");
    resultElement.appendChild(counts);
    XmlUtil.addTextNode(testResultsDocument, counts, "right", Integer.toString(testSummary.right));
    XmlUtil.addTextNode(testResultsDocument, counts, "wrong", Integer.toString(testSummary.wrong));
    XmlUtil.addTextNode(testResultsDocument, counts, "ignores", Integer.toString(testSummary.ignores));
    XmlUtil.addTextNode(testResultsDocument, counts, "exceptions", Integer.toString(testSummary.exceptions));
  }

  public void acceptOutputFirst(String output) throws Exception {
    if (response.isXmlFormat()) {
      appendHtmlToBuffer(output);
    } else {
      response.add(output);
    }
  }

  private void appendHtmlToBuffer(String output) {
    if (outputBuffer == null) {
      outputBuffer = new StringBuffer();
    }
    outputBuffer.append(output);
  }

  private void addEmptyContentMessage() throws Exception {
    response.add(formatter.messageForBlankHtml());
  }

  protected void buildHtml() throws Exception {
    PageCrawler pageCrawler = page.getPageCrawler();
    WikiPagePath fullPath = pageCrawler.getFullPath(page);
    String fullPathName = PathParser.render(fullPath);
    html = context.htmlPageFactory.newPage();
    html.title.use(pageType() + ": " + fullPathName);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(fullPathName, pageType()));
    html.actions.use(HtmlUtil.makeActions(data));
    WikiImportProperty.handleImportProperties(html, page, data);

    makeFormatter();
  }

  protected void makeFormatter() throws Exception {
    formatter = new TestHtmlFormatter(html);
  }

  protected String pageType() {
    return "Test Results";
  }

  protected String title() throws Exception {
    WikiPagePath fullPath = getPageCrawler().getFullPath(page);
    TagGroup group = new TagGroup();
    group.add(HtmlUtil.makeLink(PathParser.render(fullPath), page.getName()));
    group.add(HtmlUtil.makeItalic(pageType()));
    return group.html();
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  protected int exitCode() {
    return assertionCounts.wrong + assertionCounts.exceptions;
  }

  public static void registerListener(TestEventListener listener) {
    eventListeners.add(listener);
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public boolean isFastTest() {
    return fastTest;
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
