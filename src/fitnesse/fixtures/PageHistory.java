package fitnesse.fixtures;

import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static fitnesse.fixtures.FitnesseFixtureContext.*;
import fitnesse.responders.testHistory.TestHistory;

public class PageHistory {
  private String name;
  private Date date;
  private int right;
  private int wrong;
  private int ignores;
  private int exceptions;
  private SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);

  public void setName(String name) {
    this.name = name;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setRight(int right) {
    this.right = right;
  }

  public void setWrong(int wrong) {
    this.wrong = wrong;
  }

  public void setIgnores(int ignores) {
    this.ignores = ignores;
  }

  public void setExceptions(int exceptions) {
    this.exceptions = exceptions;
  }

  public void execute() throws IOException {
    File pageDirectory = addPageDirectory(name);
    addTestResult(pageDirectory);
  }


  private File addPageDirectory(String pageName) {
    File pageDirectory = new File(context.getTestHistoryDirectory(), pageName);
    if (!pageDirectory.exists())
      pageDirectory.mkdir();
    return pageDirectory;
  }

  private File addTestResult(File pageDirectory) throws IOException {
    String testResultFileName = String.format("%s_%d_%d_%d_%d", dateFormat.format(date), right, wrong, ignores, exceptions);
    File testResultFile = new File(pageDirectory, testResultFileName + ".xml");
    testResultFile.createNewFile();
    return testResultFile;
  }
}
