package fitnesse.responders.run;

import java.io.ByteArrayOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fitnesse.FitNesseVersion;
import fitnesse.components.XmlWriter;
import fitnesse.html.HtmlUtil;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.WikiPage;

public abstract class XmlFormatter extends BaseFormatter {

  final private Document testResultsDocument = XmlUtil.newDocument();
  private Element testResultsElement;
  private StringBuffer outputBuffer;
 
  public XmlFormatter(final WikiPage page) throws Exception {
    super(page);
  }

  @Override
  public void announceStartNewTest(WikiPage test) throws Exception {
    appendHtmlToBuffer(HtmlUtil.getHtmlOfInheritedPage("PageHeader", getPage()));
  }

  @Override
  public void announceStartTestSystem(String testSystemName, String testRunner)
      throws Exception {
    
  }

  @Override
  public void processTestOutput(String output) throws Exception {
    appendHtmlToBuffer(output);
  }

  @Override
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
  }


  
  @Override
  public void setExecutionLog(CompositeExecutionLog log) {
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





}
