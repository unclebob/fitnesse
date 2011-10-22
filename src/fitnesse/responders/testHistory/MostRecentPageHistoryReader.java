package fitnesse.responders.testHistory;

import java.io.File;
import java.text.ParseException;

public class MostRecentPageHistoryReader extends PageHistoryReader {

  TestResultRecord mostRecentRecord = null;
  File directory = null;
  
  public MostRecentPageHistoryReader(File pageDirectory) {  
    directory = pageDirectory;
  }
  
  public TestResultRecord findMostRecentTestRun() {
    try {
      readHistoryFromPageDirectory(directory);
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return mostRecentRecord;
  }
  
  @Override
  void processTestFile(TestResultRecord record) throws ParseException {
    if (mostRecentRecord == null || mostRecentRecord.getDate().compareTo(record.getDate()) < 0) {
      mostRecentRecord = record;
    }
  }
}
