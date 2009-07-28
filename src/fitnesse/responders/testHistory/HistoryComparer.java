package fitnesse.responders.testHistory;

import fitnesse.responders.run.TestExecutionReport;
import fitnesse.slimTables.HtmlTableScanner;
import org.htmlparser.util.ParserException;

import java.io.File;
import java.util.ArrayList;

public class HistoryComparer {
  // min for match is .8 content score + .2 topology bonus.
  public static final double MIN_MATCH_SCORE = .8;
  public static final double MAX_MATCH_SCORE = 1.2;
  private TableListComparer comparer;


  static class MatchedPair {
    int first;
    int second;
    public double matchScore;

    public MatchedPair(Integer first, Integer second, double matchScore) {
      this.first = first;
      this.second = second;
      this.matchScore = matchScore;
    }

    @Override
    public String toString() {
      return "[first: " + first + ", second: " + second + ", matchScore: " + matchScore +"]";
    }

    @Override
    public int hashCode() {
      return this.first + this.second;
    }

    @Override
    public boolean equals(Object obj) {
      return this.equals((MatchedPair)(obj));
    }

    public boolean equals(MatchedPair match) {
      return (this.first == match.first && this.second == match.second);
    }
  }

  public String secondFileContent = "";
  public String firstFileContent = "";
  public File resultFile;
  public static ArrayList<String> resultContent;
  public HtmlTableScanner firstScanner;
  public HtmlTableScanner secondScanner;
  public ArrayList<String> firstTableResults;
  public ArrayList<String> secondTableResults;
  public ArrayList<MatchedPair> matchedTables;
  private static final String blankTable = "<table><tr><td></td></tr></table>";

  public String getFileContent(String filePath) {
    TestExecutionReport report;
    try {
      report = new TestExecutionReport().read(new File(filePath));
      if (report.getResults().size() != 1)
        return null;
      return report.getResults().get(0).getContent();
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public double findScoreByFirstTableIndex(int firstIndex) {
    for (MatchedPair match : matchedTables) {
      if (match.first == firstIndex)
        return match.matchScore;
    }
    return 0.0;
  }

  public String findScoreByFirstTableIndexAsStringAsPercent(int firstIndex) {
    double score = findScoreByFirstTableIndex(firstIndex);
    return String.format("%10.2f", (score / MAX_MATCH_SCORE) * 100);
  }

  public boolean allTablesMatch() {
      if(matchedTables == null||matchedTables.size() == 0 || firstTableResults == null || firstTableResults.size() == 0)
        return false;
     if(matchedTables.size()== firstTableResults.size()){
       for (MatchedPair match :matchedTables){
         if(match.matchScore < (MAX_MATCH_SCORE -.01))
           return false;
       }
       return true;
     }
     return false;
   }
  
  public boolean compare(String firstFilePath, String secondFilePath) throws Exception {
    if (firstFilePath.equals(secondFilePath))
      return false;
    initializeFileContents(firstFilePath, secondFilePath);
    return grabAndCompareTablesFromHtml();
  }

  public boolean grabAndCompareTablesFromHtml() throws ParserException {
    initializeComparerHelpers();
    if (firstScanner.getTableCount() == 0 || secondScanner.getTableCount() == 0)
      return false;
    comparer = new TableListComparer(firstScanner, secondScanner);
    comparer.compareAllTables();
    matchedTables = comparer.tableMatches;
    getTableTextFromScanners();
    lineUpTheTables();
    addBlanksToUnmatchingRows();
    makePassFailResultsFromMatches();
    return true;
  }

  private void initializeComparerHelpers() throws ParserException {
    matchedTables = new ArrayList<MatchedPair>();
    resultContent = new ArrayList<String>();
    firstScanner = new HtmlTableScanner(firstFileContent);
    secondScanner = new HtmlTableScanner(secondFileContent);
  }


  public void lineUpTheTables() {
    for (int currentMatch = 0; currentMatch < matchedTables.size(); currentMatch++)
      lineUpMatch(currentMatch);
    lineUpLastRow();

  }

  private void lineUpMatch(int currentMatch) {
    insertBlanksUntilMatchLinesUp(new FirstResultAdjustmentStrategy(), currentMatch);
    insertBlanksUntilMatchLinesUp(new SecondResultAdjustmentStrategy(), currentMatch);
  }

  private void insertBlanksUntilMatchLinesUp(ResultAdjustmentStrategy adjustmentStrategy, int currentMatch) {
    while (adjustmentStrategy.matchIsNotLinedUp(currentMatch)) {
      adjustmentStrategy.insertBlankTableBefore(currentMatch);
      incrementRemaingMatchesToCompensateForInsertion(adjustmentStrategy, currentMatch);
    }
  }

  private void incrementRemaingMatchesToCompensateForInsertion(ResultAdjustmentStrategy adjustmentStrategy, int currentMatch) {
    for (int matchToAdjust = currentMatch; matchToAdjust < matchedTables.size(); matchToAdjust++) {
      matchedTables.set(matchToAdjust, adjustmentStrategy.getAdjustedMatch(matchToAdjust));
    }
  }

  private interface ResultAdjustmentStrategy {
    boolean matchIsNotLinedUp(int matchIndex);

    void insertBlankTableBefore(int matchIndex);

    MatchedPair getAdjustedMatch(int matchIndex);
  }

  private class FirstResultAdjustmentStrategy implements ResultAdjustmentStrategy {
    public boolean matchIsNotLinedUp(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return matchedPair.first < matchedPair.second;
    }

    public void insertBlankTableBefore(int matchIndex) {
      firstTableResults.add(matchedTables.get(matchIndex).first, blankTable);
    }

    public MatchedPair getAdjustedMatch(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return new MatchedPair(matchedPair.first + 1, matchedPair.second, matchedPair.matchScore);

    }
  }

  private class SecondResultAdjustmentStrategy implements ResultAdjustmentStrategy {
    public boolean matchIsNotLinedUp(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return matchedPair.first > matchedPair.second;
    }

    public void insertBlankTableBefore(int matchIndex) {
      secondTableResults.add(matchedTables.get(matchIndex).second, blankTable);
    }

    public MatchedPair getAdjustedMatch(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return new MatchedPair(matchedPair.first, matchedPair.second + 1, matchedPair.matchScore);
    }
  }

  private void lineUpLastRow() {
    while (firstTableResults.size() > secondTableResults.size())
      secondTableResults.add(blankTable);
    while (secondTableResults.size() > firstTableResults.size())
      firstTableResults.add(blankTable);
  }

  public void addBlanksToUnmatchingRows() {
    for (int tableIndex = 0; tableIndex < firstTableResults.size(); tableIndex++) {
      if (tablesDontMatchAndArentBlank(tableIndex)) {
        insetBlanksToSplitTheRow(tableIndex);
        incrementMatchedPairsIfBelowTheInsertedBlank(tableIndex);
      }
    }
  }

  private boolean tablesDontMatchAndArentBlank(int tableIndex) {
    return  !thereIsAMatchForTableWithIndex(tableIndex)&& firstAndSecondTableAreNotBlank(tableIndex);
  }

  private boolean thereIsAMatchForTableWithIndex(int tableIndex) {
    return findScoreByFirstTableIndex(tableIndex) > 0.1;
  }

  private boolean firstAndSecondTableAreNotBlank(int tableIndex) {
    return !(firstTableResults.get(tableIndex).equals(blankTable) || secondTableResults.get(tableIndex).equals(blankTable));
  }

  private void incrementMatchedPairsIfBelowTheInsertedBlank(int tableIndex) {
    for (int j = 0; j < matchedTables.size(); j++) {
      MatchedPair match = matchedTables.get(j);
      if (match.first > tableIndex)
        matchedTables.set(j, new MatchedPair(match.first + 1, match.second + 1, match.matchScore));
    }
  }

  private void insetBlanksToSplitTheRow(int tableIndex) {
    secondTableResults.add(tableIndex, blankTable);
    firstTableResults.add(tableIndex + 1, blankTable);
  }

  private void getTableTextFromScanners() {
    firstTableResults = new ArrayList<String>();
    secondTableResults = new ArrayList<String>();
    for (int i = 0; i < firstScanner.getTableCount(); i++)
      firstTableResults.add(firstScanner.getTable(i).toHtml());

    for (int i = 0; i < secondScanner.getTableCount(); i++)
      secondTableResults.add(secondScanner.getTable(i).toHtml());
  }

  public void makePassFailResultsFromMatches() {
    for (int i = 0; i < firstTableResults.size(); i++) {
      String result = "fail";
      for (MatchedPair match : matchedTables)
        if (match.first == i && match.matchScore >= 1.19)
          result = "pass";
      resultContent.add(result);

    }
  }

  private void initializeFileContents(String firstFilePath, String secondFilePath) throws ParserException {
    String content = getFileContent(firstFilePath);
    firstFileContent = content == null ? "" : content;
    content = getFileContent(secondFilePath);
    secondFileContent = content == null ? "" : content;
  }

  public ArrayList<String> getResultContent() {
    return resultContent;
  }


}
