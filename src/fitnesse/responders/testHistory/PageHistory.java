package fitnesse.responders.testHistory;

import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.XmlFormatter;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PageHistory {
  private SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);
  private int failures = 0;
  private int passes = 0;
  private Date minDate = null;
  private Date maxDate = null;
  private BarGraph barGraph;
  private final HashMap<Date, PageTestSummary> summaryMap = new HashMap<Date, PageTestSummary>();

  public PageHistory(File pageDirectory) {
    try {
      compileHistoryFromPageDirectory(pageDirectory);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void compileHistoryFromPageDirectory(File pageDirectory) throws ParseException {
    File[] resultFiles = FileUtil.getDirectoryListing(pageDirectory);
    for (File file : resultFiles)
      compileResultFileIntoHistory(file);
    compileBarGraph();
  }

  private void compileBarGraph() {
    List<Date> dates = new ArrayList<Date>(summaryMap.keySet());
    Collections.sort(dates, reverseChronologicalDateComparator());
    barGraph = new BarGraph();
    for (int i=0; i<dates.size() && i < 20; i++) {
      barGraph.addSummary(get(dates.get(i)));
    }
  }

  private Comparator<Date> reverseChronologicalDateComparator() {
    return new Comparator<Date>() {
      public int compare(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        if (diff < 0)
          return -1;
        if (diff > 0)
          return 1;
        return 0;
      }
    };
  }

  private void compileResultFileIntoHistory(File file) throws ParseException {
    String fileName = file.getName();
    PageTestSummary summary = summaryFromFilename(fileName);
    summaryMap.put(summary.getDate(), summary);
    countResult(summary);
    setMinMaxDate(summary.getDate());
  }

  private PageTestSummary summaryFromFilename(String fileName) throws ParseException {
    String parts[] = fileName.split("_|\\.");
    Date date = dateFormat.parse(parts[0]);
    PageTestSummary summary = new PageTestSummary(
      date,
      Integer.parseInt(parts[1]),
      Integer.parseInt(parts[2]),
      Integer.parseInt(parts[3]),
      Integer.parseInt(parts[4]));
    return summary;
  }

  private void setMinMaxDate(Date date) {
    if (minDate == null)
      minDate = maxDate = date;
    else if (date.getTime() > maxDate.getTime())
      maxDate = date;
    else if (date.getTime() < minDate.getTime())
      minDate = date;
  }

  private void countResult(PageTestSummary summary) {
    if (summary.wrong > 0 || summary.exceptions > 0 || summary.right == 0)
      failures++;
    else
      passes++;
  }

  public int getFailures() {
    return failures;
  }

  public int getPasses() {
    return passes;
  }

  public Date getMinDate() {
    return minDate;
  }

  public Date getMaxDate() {
    return maxDate;
  }

  public BarGraph getBarGraph() {
    return barGraph;
  }

  public int size() {
    return summaryMap.size();
  }

  public PageTestSummary get(Object key) {
    return summaryMap.get(key);
  }

  public static class PageTestSummary extends TestSummary {
    private Date date;

    PageTestSummary(Date date, int right, int wrong, int ignores, int exceptions) {
      super(right, wrong, ignores, exceptions);
      this.date = date;
    }

    public Date getDate() {
      return date;
    }
  }


  public static class BarGraph {
    private Date startingDate;
    private Date endingDate;
    private List<Boolean> passFailList = new ArrayList<Boolean>();

    public Date getStartingDate() {
      return startingDate;
    }

    public String formatStartingDate(String format) {
      return formatDate(format, startingDate);
    }

    public String formatEndingDate(String format) {
      return formatDate(format, endingDate);
    }

    private String formatDate(String format, Date date) {
      SimpleDateFormat fmt = new SimpleDateFormat(format);
      return fmt.format(date);
    }

    public Date getEndingDate() {
      return endingDate;
    }

    public void addSummary(PageTestSummary summary) {
      minMaxDate(summary);
      passFailList.add(summary.wrong == 0 && summary.exceptions == 0 && summary.right > 0);
    }

    private void minMaxDate(PageTestSummary summary) {
      Date date = summary.getDate();
      if (startingDate == null)
        startingDate = endingDate = date;
      else if (date.getTime() < startingDate.getTime())
        startingDate = date;
      else if (date.getTime() > endingDate.getTime())
        endingDate = date;
    }

    public int size() {
      return passFailList.size();
    }

    public boolean getPassFail(int i) {
      return passFailList.get(i);
    }

    public Boolean[] passFailArray() {
      return passFailList.toArray(new Boolean[passFailList.size()]);
    }
  }
}
