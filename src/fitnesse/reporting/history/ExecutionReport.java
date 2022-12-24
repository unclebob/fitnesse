package fitnesse.reporting.history;

import fitnesse.FitNesseVersion;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.util.DateTimeUtil;
import fitnesse.util.TimeMeasurement;
import fitnesse.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

public abstract class ExecutionReport {
  private static final int NO_RUN_TIME = -1;

  private String version;
  private String rootPath;
  private TestSummary finalCounts = new TestSummary(0, 0, 0, 0);
  public Date date;
  private long totalRunTimeInMillis = NO_RUN_TIME;
  private List<ExecutionLogReport> executionLogs = new ArrayList<>();

  protected ExecutionReport() {
    version = new FitNesseVersion().toString();
  }

  public ExecutionReport(FitNesseVersion version, String rootPath) {
    this.version = version == null ? "null" : version.toString();
    this.rootPath = rootPath;
  }

  public void tallyPageCounts(ExecutionResult result) {
    finalCounts.add(result);
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
    if (!StringUtils.equals(rootPath, e.rootPath))
      return false;
    else if (!StringUtils.equals(version, e.version))
      return false;
    else if (!DateTimeUtil.datesNullOrEqual(date, e.date))
      return false;
    else if(!finalCounts.equals(e.finalCounts))
      return false;
    else if(totalRunTimeInMillis != e.totalRunTimeInMillis)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(rootPath).append(version).append(date).hashCode();
  }

  public static ExecutionReport makeReport(String xmlString) throws InvalidReportException {
    Document xmlDocument = null;
    try {
      xmlDocument = XmlUtil.newDocument(xmlString);
    } catch (IOException | SAXException e) {
      throw new InvalidReportException(format("%s is not a valid execution report", xmlString), e);
    }
    Element documentElement = xmlDocument.getDocumentElement();
    String documentNodeName = documentElement.getNodeName();
    switch (documentNodeName) {
      case "testResults":
        return new TestExecutionReport(xmlDocument);
      case "suiteResults":
        return new SuiteExecutionReport(xmlDocument);
      default:
        throw new InvalidReportException(format("%s is not a valid document element tag for an Execution Report.", documentNodeName));
    }
  }

  protected void unpackCommonFields(Element documentElement) throws InvalidReportException {
    version = XmlUtil.getTextValue(documentElement, "FitNesseVersion");
    rootPath = XmlUtil.getTextValue(documentElement, "rootPath");
    String dateString = XmlUtil.getTextValue(documentElement, "date");
    if (dateString != null)
      try {
        date = DateTimeUtil.getDateFromString(dateString);
      } catch (ParseException e) {
        throw new InvalidReportException(format("'%s' is not a valid date.", dateString), e);
      }
    unpackFinalCounts(documentElement);
    totalRunTimeInMillis = getTotalRunTimeInMillisOrMinusOneIfNotPresent(documentElement);
  }

  protected long getTotalRunTimeInMillisOrMinusOneIfNotPresent(Element documentElement) {
    String textValue = XmlUtil.getTextValue(documentElement, "totalRunTimeInMillis");
    return textValue == null ? NO_RUN_TIME : Long.parseLong(textValue);
  }

  private void unpackFinalCounts(Element testResults) {
    Element counts = XmlUtil.getElementByTagName(testResults, "finalCounts");
    if (counts != null) {
      finalCounts = new TestSummary(
        Integer.parseInt(XmlUtil.getTextValue(counts, "right")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "wrong")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "ignores")),
        Integer.parseInt(XmlUtil.getTextValue(counts, "exceptions"))
      );
    }
  }

  protected void unpackXml(Document xmlDoc) throws InvalidReportException {
    Element historyDocument = xmlDoc.getDocumentElement();
    unpackCommonFields(historyDocument);
    unpackResults(historyDocument);
    unpackExecutionLogs(historyDocument);
  }

  private void unpackExecutionLogs(Element historyDocument) {
    NodeList logs = historyDocument.getElementsByTagName("executionLog");
    if (logs == null) {
      return;
    }
    for (int i = 0; i < logs.getLength(); i++) {
      Element log = (Element) logs.item(i);
      String commandLine = XmlUtil.getTextValue(log, "command");
      String testSystemName = XmlUtil.getTextValue(log, "testSystem");
      String exitCode = XmlUtil.getTextValue(log, "exitCode");
      String stdOut = XmlUtil.getTextValue(log, "stdOut");
      String stdErr = XmlUtil.getTextValue(log, "stdErr");

      ExecutionLogReport report = new ExecutionLogReport(commandLine, testSystemName);
      if (StringUtils.isNotBlank(exitCode)) {
        report.exitCode(Integer.parseInt(exitCode));
      }
      if (stdOut != null) {
        report.setStdOut(stdOut);
      }
      if (stdErr != null) {
        report.setStdErr(stdErr);
      }

      NodeList exceptionNodes = log.getElementsByTagName("exception");
      if (exceptionNodes != null) {
        for (int k = 0; k < exceptionNodes.getLength(); k++) {
          report.exceptionOccurred(new Exception(exceptionNodes.item(k).getTextContent()));
        }
      }
      executionLogs.add(report);
    }
  }

  protected abstract void unpackResults(Element testResults) throws InvalidReportException;

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

  public String getDateString() {
    return DateTimeUtil.formatDate(date);
  }

  public String getResultDate() {
    SimpleDateFormat pageHistoryFormatter = PageHistory.getDateFormat();
    return pageHistoryFormatter.format(date);
  }

  public boolean hasRunTimes() {
    return totalRunTimeInMillis != NO_RUN_TIME;
  }

  public List<ExecutionLogReport> getExecutionLogs() {
    return new ArrayList<>(executionLogs);
  }

  public void addExecutionContext(String command, String testSystemName) {
    executionLogs.add(new ExecutionLogReport(command, testSystemName));
  }

  private ExecutionLogReport executionLogReport() {
    ExecutionLogReport log;
    if (!executionLogs.isEmpty()) {
      log = executionLogs.get(executionLogs.size() - 1);
    } else {
      log = new ExecutionLogReport("", "");
      executionLogs.add(log);
    }
    return log;
  }

  public void addStdOut(String output) {
    executionLogReport().addStdOut(output);
  }

  public void addStdErr(String output) {
    executionLogReport().addStdErr(output);
  }

  public void exitCode(int exitCode) {
    executionLogReport().exitCode(exitCode);
  }

  public void exceptionOccurred(Throwable e) {
    executionLogReport().exceptionOccurred(e);
  }

  public static class ExecutionLogReport {
    private final String command;
    private final String testSystemName;
    private StringBuffer stdOut = new StringBuffer();
    private StringBuffer stdErr = new StringBuffer();
    private int exitCode;
    private List<Throwable> exceptions = new ArrayList<>();

    public ExecutionLogReport(String command, String testSystemName) {
      this.command = command;
      this.testSystemName = testSystemName;
    }

    public String getCommand() {
      return command;
    }

    public String getTestSystemName() {
      return testSystemName;
    }

    public void addStdOut(String output) {
      stdOut.append(output).append("\n");
    }

    public void setStdOut(String output) {
      this.stdOut.append(output);
    }

    public String getStdOut() {
      return stdOut.toString();
    }

    public void addStdErr(String output) {
      stdErr.append(output).append("\n");
    }

    public void setStdErr(String output) {
      this.stdErr.append(output);
    }

    public String getStdErr() {
      return stdErr.toString();
    }

    public void exitCode(int exitCode) {
      this.exitCode = exitCode;
    }

    public int getExitCode() {
      return exitCode;
    }

    public void exceptionOccurred(Throwable e) {
      exceptions.add(e);
    }

    public List<Throwable> getExceptions() {
      return new ArrayList<>(exceptions);
    }
  }
}
