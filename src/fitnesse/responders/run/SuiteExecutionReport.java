package fitnesse.responders.run;

import fitnesse.responders.testHistory.TestHistory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.DateTimeUtil;
import util.StringUtil;
import util.XmlUtil;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SuiteExecutionReport extends ExecutionReport {
  private List<PageHistoryReference> pageHistoryReferences = new ArrayList<PageHistoryReference>();

  public SuiteExecutionReport(Document xmlDocument) throws Exception {
    super(xmlDocument);
    unpackXml();
  }

  public SuiteExecutionReport() {
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SuiteExecutionReport) {
      SuiteExecutionReport report = (SuiteExecutionReport) o;
      if (!super.equals(report))
        return false;
      if (pageHistoryReferences.size() != report.pageHistoryReferences.size())
        return false;
      else if (!allReferencesEqual(pageHistoryReferences, report.pageHistoryReferences))
        return false;
      else
        return true;
    }
    return false;
  }

  private boolean allReferencesEqual(List<PageHistoryReference> r1, List<PageHistoryReference> r2) {
    for (int i=0; i<r1.size(); i++) {
      if (!r1.get(i).equals(r2.get(i)))
        return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format("SuiteExecutionReport({%s}{%s})", super.toString(), pageHistoryReferencesToString());
  }

  private String pageHistoryReferencesToString() {
    StringBuilder builder = new StringBuilder();
    if (pageHistoryReferences.size() > 0) {
      for (PageHistoryReference reference : pageHistoryReferences) {
        builder.append(reference.toString());
        builder.append(",");
      }
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  public void toXml(Writer writer, VelocityEngine velocityEngine) throws Exception {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("suiteExecutionReport", this);
    Template template = velocityEngine.getTemplate("suiteHistoryXML.vm");
    template.merge(velocityContext, writer);
  }

  protected void unpackResults(Element testResults) throws Exception {
    NodeList references = testResults.getElementsByTagName("pageHistoryReference");
    for (int referenceIndex = 0;referenceIndex < references.getLength();referenceIndex++){
      Element refElement = (Element) references.item(referenceIndex);
      String name = XmlUtil.getTextValue(refElement,"name");
      long time = DateTimeUtil.getTimeFromString(XmlUtil.getTextValue(refElement,"date"));
      long runTimeInMillis = getRunTimeInMillisOrZeroIfNotPresent(refElement);
      PageHistoryReference r1 = new PageHistoryReference(name,time,runTimeInMillis);
      Element counts = XmlUtil.getElementByTagName(refElement,"counts");
      r1.getTestSummary().right = new Integer(XmlUtil.getTextValue(counts,"right"));
      r1.getTestSummary().wrong = new Integer(XmlUtil.getTextValue(counts,"wrong"));
      r1.getTestSummary().ignores = new Integer(XmlUtil.getTextValue(counts,"ignores"));
      r1.getTestSummary().exceptions = new Integer(XmlUtil.getTextValue(counts,"exceptions"));
      pageHistoryReferences.add(r1);
    }
  }

  protected long getRunTimeInMillisOrZeroIfNotPresent(Element refElement) throws Exception {
    String textValue = XmlUtil.getTextValue(refElement, "runTimeInMillis");
    return textValue == null ? 0 : Long.parseLong(textValue);
  }

  public List<PageHistoryReference> getPageHistoryReferences() {
    return pageHistoryReferences;
  }

  public void addPageHistoryReference(PageHistoryReference reference) {
    pageHistoryReferences.add(reference);
  }

  public void tallyPageCounts(TestSummary testSummary) {
    finalCounts.tallyPageCounts(testSummary);
  }
  
  public static class PageHistoryReference {
    private String pageName;
    private long time;
    private long runTimeInMillis;
    private TestSummary testSummary = new TestSummary();

    public PageHistoryReference(String pageName, long time, long runTimeInMillis) {
      this.pageName = pageName;
      this.time = time;
      this.runTimeInMillis = runTimeInMillis;
    }

    @Override
    public String toString() {
      return String.format("[%s, %s, %s, %s]", pageName, DateTimeUtil.formatDate(new Date(time)), testSummary, runTimeInMillis);
    }

    @Override
    public boolean equals(Object o) {
      if (! (o instanceof PageHistoryReference))
        return false;
      PageHistoryReference r = (PageHistoryReference) o;
      return StringUtil.stringsNullOrEqual(pageName, r.pageName) &&
        time == r.time &&
        testSummary.equals(r.testSummary) &&
        runTimeInMillis == r.runTimeInMillis;
    }

    public String getPageName() {
      return pageName;
    }

    public long getTime() {
      return time;
    }

    public long getRunTimeInMillis() {
      return runTimeInMillis;
    }

    public void setRunTimeInMillis(long runTimeInMillis) {
      this.runTimeInMillis = runTimeInMillis;
    }

    public String getDateString() {
      return DateTimeUtil.formatDate(new Date(time));
    }

    public String getResultDate() {
      SimpleDateFormat pageHistoryFormatter = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
      return pageHistoryFormatter.format(new Date(time));
    }

    public TestSummary getTestSummary() {
      return testSummary;
    }

    public void setTestSummary(TestSummary testSummary) {
      this.testSummary = new TestSummary(testSummary);
    }
  }

}
