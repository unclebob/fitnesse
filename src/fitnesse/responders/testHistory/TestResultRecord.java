package fitnesse.responders.testHistory;

import java.io.File;
import java.util.Date;

import fitnesse.responders.run.TestSummary;

public class TestResultRecord extends TestSummary{
  private File file;
  private Date date;

  TestResultRecord(File file, Date date, int right, int wrong, int ignores, int exceptions) {
    super(right, wrong, ignores, exceptions);
    this.file = file;
    this.date = date;
  }

  public Date getDate() {
    return date;
  }

  public File getFile() {
    return file;
  }
}
