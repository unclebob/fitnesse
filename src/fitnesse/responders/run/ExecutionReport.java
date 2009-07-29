package fitnesse.responders.run;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.StringUtil;
import util.XmlUtil;
import util.DateTimeUtil;

import java.util.Date;

public abstract class ExecutionReport {
  public String version;
  public String rootPath;
  public TestSummary finalCounts = new TestSummary(0, 0, 0, 0);
  public Date date;
  protected Document xmlDoc;

  protected ExecutionReport(Document xmlDocument) throws Exception {
    xmlDoc = xmlDocument;
  }

  protected ExecutionReport() {
  }

  @Override
  public String toString() {
    return rootPath;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ExecutionReport))
      return false;
    ExecutionReport e = (ExecutionReport) o;
    if (!StringUtil.stringsNullOrEqual(rootPath, e.rootPath))
      return false;
    else if (!StringUtil.stringsNullOrEqual(version, e.version))
      return false;
    else if (!DateTimeUtil.datesNullOrEqual(date, e.date))
      return false;
    else if(!finalCounts.equals(e.finalCounts))
      return false;
    return true;
  }

  public static ExecutionReport makeReport(String xmlString) throws Exception {
    Document xmlDocument = XmlUtil.newDocument(xmlString);
    Element documentElement = xmlDocument.getDocumentElement();
    String documentNodeName = documentElement.getNodeName();
    if (documentNodeName.equals("testResults"))
      return new TestExecutionReport(xmlDocument);
    else if (documentNodeName.equals("suiteResults"))
      return new SuiteExecutionReport(xmlDocument);
    else
      throw new RuntimeException(String.format("%s is not a valid document element tag for an Execution Report.", documentNodeName));
  }

  protected void unpackCommonFields(Element documentElement) throws Exception {
    version = XmlUtil.getTextValue(documentElement, "FitNesseVersion");
    rootPath = XmlUtil.getTextValue(documentElement, "rootPath");
    String dateString = XmlUtil.getTextValue(documentElement, "date");
    if (dateString != null)
      date = DateTimeUtil.getDateFromString(dateString);
    unpackFinalCounts(documentElement);
  }

  private void unpackFinalCounts(Element testResults) throws Exception {
    Element counts = util.XmlUtil.getElementByTagName(testResults, "finalCounts");
    if (counts != null) {
      finalCounts = new TestSummary(
        Integer.parseInt(XmlUtil.getTextValue(counts, "right")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "wrong")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "ignores")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "exceptions"))
      );
    }
  }

  protected void unpackXml() throws Exception {
    Element historyDocument = xmlDoc.getDocumentElement();
    unpackCommonFields(historyDocument);
    unpackResults(historyDocument);
  }

  protected abstract void unpackResults(Element testResults) throws Exception;

  public TestSummary getFinalCounts() {
    return finalCounts;
  }

  public String getVersion() {
    return version;
  }

  public String getRootPath() {
    return rootPath;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
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
}
