package fitnesse.responders.testHistory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import fitnesse.reporting.history.InvalidReportException;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.testsystems.slim.HtmlTableScanner;

public class HistoryComparer {
  // min for match is .8 content score + .2 topology bonus.
  static final double MIN_MATCH_SCORE = .8;
  static final double MAX_MATCH_SCORE = 1.2;

  private static final String blankTable = "<table><tr><td></td></tr></table>";

  private HtmlTableScanner firstScanner;
  private HtmlTableScanner secondScanner;

  String firstFileContent = "";
  String secondFileContent = "";

  List<String> firstTableResults = new ArrayList<>();
  List<String> secondTableResults = new ArrayList<>();

  List<MatchedPair> matchedTables = new ArrayList<>();
  List<String> resultContent = new ArrayList<>();

  public String getFileContent(String filePath) throws IOException, SAXException, InvalidReportException {
    return attemptGetFileContent(filePath);
  }

  private String attemptGetFileContent(String filePath) throws IOException, SAXException, InvalidReportException {
    TestExecutionReport report = readTestExecutionReport(filePath);
    if (!exactlyOneReport(report))
      return null;
    return report.getContentsOfReport(0);
  }

  private TestExecutionReport readTestExecutionReport(String filePath) throws IOException, SAXException, InvalidReportException {
    return new TestExecutionReport(new File(filePath));
  }

  private boolean exactlyOneReport(TestExecutionReport report) {
    return report.getResults().size() == 1;
  }

  public double findScoreByFirstTableIndex(int firstIndex) {
    for (MatchedPair match : matchedTables)
      if (match.first == firstIndex)
        return match.matchScore;

    return 0.0;
  }

  public String findScoreByFirstTableIndexAsStringAsPercent(int firstIndex) {
    double score = findScoreByFirstTableIndex(firstIndex);
    return String.format("%10.2f", (score / MAX_MATCH_SCORE) * 100);
  }

  public boolean allTablesMatch() {
    return matchesAreNotNull()
        && thereAreEnoughMatches()
        && allMatchScoresAreHigh();
  }


  private boolean matchesAreNotNull() {
    return matchedTables != null && firstTableResults != null;
  }

  private boolean thereAreEnoughMatches() {
    return !matchedTables.isEmpty() && matchedTables.size() == firstTableResults.size();
  }

  private boolean allMatchScoresAreHigh() {
    for (MatchedPair match : matchedTables) {
      if (match.matchScore < (MAX_MATCH_SCORE - .01))
        return false;
    }
    return true;
  }

  public boolean compare(String firstFilePath, String secondFilePath) throws IOException, SAXException, InvalidReportException {
    if (firstFilePath.equals(secondFilePath))
      return false;
    initializeFileContents(firstFilePath, secondFilePath);
    return grabAndCompareTablesFromHtml();
  }

  public boolean grabAndCompareTablesFromHtml() {
    initializeComparerHelpers();
    if (firstScanner.getTableCount() == 0 || secondScanner.getTableCount() == 0)
      return false;
    TableListComparer comparer = new TableListComparer(firstScanner, secondScanner);
    comparer.compareAllTables();
    matchedTables = comparer.tableMatches;
    getTableTextFromScanners();
    lineUpTheTables();
    addBlanksToUnmatchingRows();
    makePassFailResultsFromMatches();
    return true;
  }

  private void initializeComparerHelpers() {
    matchedTables = new ArrayList<>();
    resultContent = new ArrayList<>();
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
    @Override
    public boolean matchIsNotLinedUp(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return matchedPair.first < matchedPair.second;
    }

    @Override
    public void insertBlankTableBefore(int matchIndex) {
      firstTableResults.add(matchedTables.get(matchIndex).first, blankTable);
    }

    @Override
    public MatchedPair getAdjustedMatch(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return new MatchedPair(matchedPair.first + 1, matchedPair.second, matchedPair.matchScore);

    }
  }

  private class SecondResultAdjustmentStrategy implements ResultAdjustmentStrategy {
    @Override
    public boolean matchIsNotLinedUp(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return matchedPair.first > matchedPair.second;
    }

    @Override
    public void insertBlankTableBefore(int matchIndex) {
      secondTableResults.add(matchedTables.get(matchIndex).second, blankTable);
    }

    @Override
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
    return !thereIsAMatchForTableWithIndex(tableIndex) && firstAndSecondTableAreNotBlank(tableIndex);
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
    firstTableResults = new ArrayList<>();
    secondTableResults = new ArrayList<>();
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

  private void initializeFileContents(String firstFilePath, String secondFilePath) throws IOException, SAXException, InvalidReportException {
    String content = getFileContent(firstFilePath);
    firstFileContent = content == null ? "" : content;
    content = getFileContent(secondFilePath);
    secondFileContent = content == null ? "" : content;
  }

  public List<String> getResultContent() {
    return resultContent;
  }

  static class MatchedPair {
    int first;
    int second;
    double matchScore;

    public MatchedPair(Integer first, Integer second, double matchScore) {
      this.first = first;
      this.second = second;
      this.matchScore = matchScore;
    }

    @Override
    public String toString() {
      return "[first: " + first + ", second: " + second + ", matchScore: " + matchScore + "]";
    }

    @Override
    public int hashCode() {
      return this.first + this.second;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      MatchedPair match = (MatchedPair) obj;
      return (this.first == match.first && this.second == match.second);
    }
  }
}
