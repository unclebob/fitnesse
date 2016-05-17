package fitnesse.reporting.history;

import java.io.File;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MostRecentPageHistoryReader extends PageHistoryReader {
  private static final Logger LOG = Logger.getLogger(MostRecentPageHistoryReader.class.getName());

  TestResultRecord mostRecentRecord = null;
  File directory = null;
  
  public MostRecentPageHistoryReader(File pageDirectory) {  
    directory = pageDirectory;
  }
  
  public TestResultRecord findMostRecentTestRun() {
    try {
      readHistoryFromPageDirectory(directory);
    } catch (ParseException e) {
      LOG.log(Level.WARNING, "Unable to read history from page directory", e);
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
