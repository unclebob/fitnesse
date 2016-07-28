package fitnesse.reporting.history;

import java.io.File;

public class MostRecentPageHistoryReader extends PageHistoryReader {
  TestResultRecord mostRecentRecord = null;
  File directory = null;

  public MostRecentPageHistoryReader(File pageDirectory) {
    directory = pageDirectory;
  }

  public TestResultRecord findMostRecentTestRun() {
    readHistoryFromPageDirectory(directory);
    return mostRecentRecord;
  }

  @Override
  void processTestFile(TestResultRecord record) {
    if (mostRecentRecord == null || mostRecentRecord.getDate().compareTo(record.getDate()) < 0) {
      mostRecentRecord = record;
    }
  }
}
