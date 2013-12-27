package fitnesse.reporting.history;

import java.util.Date;

import fitnesse.testsystems.ExecutionResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fitnesse.FitNesseVersion;
import fitnesse.testsystems.TestSummary;
import util.DateTimeUtil;
import util.StringUtil;
import util.TimeMeasurement;
import util.XmlUtil;

public abstract class ExecutionReport {
  private String version;
  private String rootPath;
  private TestSummary finalCounts = new TestSummary(0, 0, 0, 0);
  public Date date;
  private long totalRunTimeInMillis = 0;

  protected ExecutionReport() {
    version = new FitNesseVersion().toString();
  }

  public ExecutionReport(FitNesseVersion version, String rootPath) {
    this.version = version == null ? "null" : version.toString();
    this.rootPath = rootPath;
  }

  public void tallyPageCounts(ExecutionResult result) {
    finalCounts.tallyPageCounts(result);
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
    else if(totalRunTimeInMillis != e.totalRunTimeInMillis)
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

  protected void unpackCommonFields(Element documentElement) {
    version = XmlUtil.getTextValue(documentElement, "FitNesseVersion");
    rootPath = XmlUtil.getTextValue(documentElement, "rootPath");
    String dateString = XmlUtil.getTextValue(documentElement, "date");
    if (dateString != null)
      date = DateTimeUtil.getDateFromString(dateString);
    unpackFinalCounts(documentElement);
    totalRunTimeInMillis = getTotalRunTimeInMillisOrZeroIfNotPresent(documentElement);
  }

  protected long getTotalRunTimeInMillisOrZeroIfNotPresent(Element documentElement) {
    String textValue = XmlUtil.getTextValue(documentElement, "totalRunTimeInMillis");
    return textValue == null ? 0 : Long.parseLong(textValue);
  }

  private void unpackFinalCounts(Element testResults) {
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

  protected void unpackXml(Document xmlDoc) {
    Element historyDocument = xmlDoc.getDocumentElement();
    unpackCommonFields(historyDocument);
    unpackResults(historyDocument);
  }

  protected abstract void unpackResults(Element testResults);

  public TestSummary getFinalCounts() {
    return finalCounts;
  }

  public String getVersion() {
    return version;
  }

  public long getTotalRunTimeInMillis() {
    return totalRunTimeInMillis;
  }
  
  public void setTotalRunTimeInMillis(TimeMeasurement totalTimeMeasurement) {
    totalRunTimeInMillis = totalTimeMeasurement.elapsed();
  }

  public String getRootPath() {
    return rootPath;
  }

  public Date getDate() {
    return new Date(date.getTime());
  }

  public void setDate(Date date) {
    this.date = new Date(date.getTime());
  }
	  
  public boolean hasRunTimes() {
    return new FitNesseVersion(getVersion()).isAtLeast("v20100607");
  }
}
