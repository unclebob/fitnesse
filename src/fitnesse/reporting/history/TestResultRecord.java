package fitnesse.reporting.history;

import java.io.File;
import java.util.Date;

import fitnesse.testsystems.TestSummary;

public class TestResultRecord {
  private final File file;
  private final Date date;
  private final int right;
  private final int wrong;
  private final int ignores;
  private final int exceptions;

  public TestResultRecord(File file, Date date, int right, int wrong, int ignores, int exceptions) {
    this.file = file;
    this.date = date;
    this.right = right;
    this.wrong = wrong;
    this.ignores = ignores;
    this.exceptions = exceptions;
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

  public int getRight() {
    return right;
  }

  public int getWrong() {
    return wrong;
  }

  public int getIgnores() {
    return ignores;
  }

  public int getExceptions() {
    return exceptions;
  }

  public TestSummary toTestSummary() {
    return new TestSummary(right, wrong, ignores, exceptions);
  }
}
