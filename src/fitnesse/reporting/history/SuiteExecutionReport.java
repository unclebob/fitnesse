package fitnesse.reporting.history;

import fitnesse.FitNesseVersion;
import fitnesse.testsystems.TestSummary;

import fitnesse.wiki.PathParser;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import fitnesse.util.DateTimeUtil;
import fitnesse.util.XmlUtil;

import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

public class SuiteExecutionReport extends ExecutionReport {
  private List<PageHistoryReference> pageHistoryReferences = new ArrayList<>();

  public SuiteExecutionReport(Document xmlDocument) throws InvalidReportException {
    super();
    unpackXml(xmlDocument);
  }

  public SuiteExecutionReport(FitNesseVersion version, String rootPath) {
    super(version, rootPath);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SuiteExecutionReport) {
      SuiteExecutionReport report = (SuiteExecutionReport) o;
      if (!super.equals(report))
        return false;
      if (pageHistoryReferences.size() != report.pageHistoryReferences.size())
        return false;
      else
        return allReferencesEqual(pageHistoryReferences, report.pageHistoryReferences);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return pageHistoryReferences.size();
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
    return format("SuiteExecutionReport({%s}{%s})", super.toString(), pageHistoryReferencesToString());
  }

  private String pageHistoryReferencesToString() {
    StringBuilder builder = new StringBuilder();
    if (!pageHistoryReferences.isEmpty()) {
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

  @Override
  protected void unpackResults(Element testResults) throws InvalidReportException {
    NodeList references = testResults.getElementsByTagName("pageHistoryReference");
    for (int referenceIndex = 0;referenceIndex < references.getLength();referenceIndex++){
      Element refElement = (Element) references.item(referenceIndex);
      String name = XmlUtil.getTextValue(refElement,"name");
      long time = 0;
      String dateString = XmlUtil.getTextValue(refElement, "date");
      try {
        time = DateTimeUtil.getTimeFromString(dateString);
      } catch (ParseException e) {
        throw new InvalidReportException(format("'%s' is not a valid date", dateString), e);
      }
      long runTimeInMillis = getRunTimeInMillisOrZeroIfNotPresent(refElement);
      PageHistoryReference r1 = new PageHistoryReference(name,time,runTimeInMillis);
      Element counts = XmlUtil.getElementByTagName(refElement,"counts");

      r1.setTestSummary(new TestSummary(
              Integer.valueOf(XmlUtil.getTextValue(counts, "right")),
              Integer.valueOf(XmlUtil.getTextValue(counts, "wrong")),
              Integer.valueOf(XmlUtil.getTextValue(counts, "ignores")),
              Integer.valueOf(XmlUtil.getTextValue(counts, "exceptions"))));
      pageHistoryReferences.add(r1);
    }
  }

  protected long getRunTimeInMillisOrZeroIfNotPresent(Element refElement) {
    String textValue = XmlUtil.getTextValue(refElement, "runTimeInMillis");
    return textValue == null ? 0 : Long.parseLong(textValue);
  }

  public List<PageHistoryReference> getPageHistoryReferences() {
    return pageHistoryReferences;
  }

  public void addPageHistoryReference(PageHistoryReference reference) {
    pageHistoryReferences.add(reference);
  }

  public static class PageHistoryReference {
    private String pageName;
    private long time;
    private long runTimeInMillis;
    private TestSummary testSummary = new TestSummary();

    public PageHistoryReference(String pageName, long time) {
      this(pageName, time, 0);
    }

    public PageHistoryReference(String pageName, long time, long runTimeInMillis) {
      this.pageName = pageName;
      this.time = time;
      this.runTimeInMillis = runTimeInMillis;
    }

    @Override
    public String toString() {
      return format("[%s, %s, %s, %s]", pageName, DateTimeUtil.formatDate(new Date(time)), testSummary, runTimeInMillis);
    }

    @Override
    public boolean equals(Object o) {
      if (! (o instanceof PageHistoryReference))
        return false;
      PageHistoryReference r = (PageHistoryReference) o;
      return StringUtils.equals(pageName, r.pageName) &&
        time == r.time &&
        testSummary.equals(r.testSummary) &&
        runTimeInMillis == r.runTimeInMillis;
    }

    @Override
    public int hashCode() {
      return pageName.hashCode();
    }

    public String getPageName() {
      return pageName;
    }

    public String getRelativePageName() {
      return PathParser.parse(pageName).last();
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
      SimpleDateFormat pageHistoryFormatter = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
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
