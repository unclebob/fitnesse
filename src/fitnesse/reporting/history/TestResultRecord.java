package fitnesse.reporting.history;

import java.io.File;
import java.util.Date;

import fitnesse.testsystems.TestSummary;

public class TestResultRecord extends TestSummary {
  private File file;
  private Date date;

  public TestResultRecord(File file, Date date, int right, int wrong, int ignores, int exceptions) {
    super(right, wrong, ignores, exceptions);
    this.file = file;
    this.date = date;
  }

  public Date getDate() {
    return new Date(date.getTime());
  }

  public File getFile() {
    return file;
  }

  public String getWikiPageName() {
    return file.getParentFile().getName();
  }
}
