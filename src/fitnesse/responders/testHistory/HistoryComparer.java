package fitnesse.responders.testHistory;

import com.sun.tools.javac.util.Pair;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.slimTables.HtmlTableScanner;
import org.htmlparser.util.ParserException;

import java.io.File;
import java.util.ArrayList;

public class HistoryComparer {
  // min for match is .8 content score + .2 topology bonus.
  public static final double MIN_MATCH_SCORE = 1.0;
  private TableListComparer comparer;

  static class MatchedPair extends Pair<Integer, Integer> {
    public double matchScore;
    public MatchedPair(Integer first, Integer second, double matchScore) {
      super(first, second);
      this.matchScore = matchScore;
    }
    public boolean equals(MatchedPair match){
      return (this.fst == match.fst && this.snd == match.snd);
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
  private static final String blankTable = "<table><tr><td>nothing</td></tr></table>";

  public String getFileContent(String filePath) {
    TestExecutionReport report;
    try {
      report = new TestExecutionReport(new File(filePath));
      return report.getResults().get(0).getContent();
    } catch (Exception e) {
      throw new RuntimeException();
    }
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
    comparer = new TableListComparer(firstScanner,secondScanner);
    comparer.compareAllTables();
    matchedTables = comparer.tableMatches;
    getTableTextFromScanners();
    lineUpTheTables();
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
      return matchedPair.fst < matchedPair.snd;
    }

    public void insertBlankTableBefore(int matchIndex) {
      firstTableResults.add(matchedTables.get(matchIndex).fst, blankTable);
    }

    public MatchedPair getAdjustedMatch(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return new MatchedPair(matchedPair.fst + 1, matchedPair.snd, matchedPair.matchScore);

    }
  }

  private class SecondResultAdjustmentStrategy implements ResultAdjustmentStrategy {
    public boolean matchIsNotLinedUp(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return matchedPair.fst > matchedPair.snd;
    }

    public void insertBlankTableBefore(int matchIndex) {
      secondTableResults.add(matchedTables.get(matchIndex).snd, blankTable);
    }

    public MatchedPair getAdjustedMatch(int matchIndex) {
      MatchedPair matchedPair = matchedTables.get(matchIndex);
      return new MatchedPair(matchedPair.fst, matchedPair.snd + 1, matchedPair.matchScore);
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
    boolean tablesDontMatch = true;
    for(MatchedPair match : matchedTables){
        if(match.equals(new MatchedPair(tableIndex, tableIndex, 0)))
          tablesDontMatch = false;
    }
     return tablesDontMatch && firstAndSecondTableAreNotBlank(tableIndex);
  }

  private boolean firstAndSecondTableAreNotBlank(int tableIndex) {
    return !(firstTableResults.get(tableIndex).equals(blankTable) || secondTableResults.get(tableIndex).equals(blankTable));
  }

  private void incrementMatchedPairsIfBelowTheInsertedBlank(int tableIndex) {
    for (int j = 0; j < matchedTables.size(); j++) {
      MatchedPair match = matchedTables.get(j);
      if (match.fst > tableIndex)
        matchedTables.set(j, new MatchedPair(match.fst + 1, match.snd + 1, match.matchScore));
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
        if (match.fst == i && match.matchScore >= 1.19)
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
