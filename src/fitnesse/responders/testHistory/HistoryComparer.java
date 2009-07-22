package fitnesse.responders.testHistory;

import com.sun.tools.javac.util.Pair;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import org.htmlparser.util.ParserException;

import java.io.File;
import java.util.ArrayList;

public class HistoryComparer {
  private static final double MIN_MATCH_SCORE = 1.0;

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
    compareAllTables();
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

  private void compareAllTables() {
    for (int firstTableIndex = 0; firstTableIndex < firstScanner.getTableCount(); firstTableIndex++) {
      for (int secondTableIndex = 0; secondTableIndex < secondScanner.getTableCount(); secondTableIndex++)  {
        double score = compareTables(firstScanner.getTable(firstTableIndex), secondScanner.getTable(secondTableIndex));
        setMatchIfItIsTheTablesBestMatch(firstTableIndex, secondTableIndex, score);
      }
    }
  }

  public void setMatchIfItIsTheTablesBestMatch(int firstTableIndex, int secondTableIndex, double score) {
    if(!theTablesMatch(score))
      return;
    boolean foundFirstMatch = false;
    boolean foundSecondMatch = false;
    int firstTablesBetterMatch = -1;
    int secondTablesBetterMatch  = -1;
    for(int matchIndex = 0;matchIndex < matchedTables.size();matchIndex++){
       if(matchedTables.get(matchIndex).fst == firstTableIndex){
         foundFirstMatch = true;
         if(matchedTables.get(matchIndex).matchScore < score)
          firstTablesBetterMatch = matchIndex;
       }
       if(matchedTables.get(matchIndex).snd == secondTableIndex){
         foundSecondMatch = true;
         if(matchedTables.get(matchIndex).matchScore < score)
           secondTablesBetterMatch = matchIndex;
       }
    }
    if (foundFirstMatch && firstTablesBetterMatch != -1)
      if(!foundSecondMatch || secondTablesBetterMatch != -1){
        matchedTables.set(firstTablesBetterMatch, new MatchedPair(firstTableIndex,secondTableIndex, score));
       if(secondTablesBetterMatch != -1)
       matchedTables.remove(secondTablesBetterMatch);
     }
    if (foundSecondMatch && secondTablesBetterMatch != -1)
      if(!foundFirstMatch)
        matchedTables.set(secondTablesBetterMatch, new MatchedPair(firstTableIndex,secondTableIndex, score));

    if (!foundFirstMatch && !foundSecondMatch)
      matchedTables.add(new MatchedPair(firstTableIndex,secondTableIndex, score));
  }


  public static double compareTables(Table table1, Table table2) {

    return compareRowsOfTables(table1, table2);
  }

  private static double compareRowsOfTables(Table table1, Table table2) {
    double score =0;
    double colsScore = 0;
    int cellCountForTable1 = 0;
    int cellCountForTable2 = 0;
    int table1rows = table1.getRowCount();
    int table2rows = table2.getRowCount();
    if (table1rows != table2rows)
      return 0;
    for (int row = 0; row < table1rows; row++) {
      int table1Cols = table1.getColumnCountInRow(row);
      int table2Cols = table2.getColumnCountInRow(row);
      cellCountForTable1 += table1Cols;
      cellCountForTable2 += table2Cols;
      if (table1Cols == table2Cols )
        score += .1 * (2.0/(table1rows + table2rows));
      colsScore += compareCellsInRow(table1, table2, row);
    }
    score += (colsScore * 2) / (table1rows + table2rows);
    if (cellCountForTable1 == cellCountForTable2)
    score += .1;
    return score;
  }

  private static double compareCellsInRow(Table table1, Table table2, int row) {
    int table1Cols = table1.getColumnCountInRow(row);
    int table2Cols = table2.getColumnCountInRow(row);
    double score = 0;
    int minNumberOfCols = table1Cols <= table2Cols ? table1Cols : table2Cols;
    for (int j = 0; j < minNumberOfCols; j++) {
      String content1 = table1.getCellResult(j, row);
      String content2 = table2.getCellResult(j, row);
      if (content1.equals(content2))
        score += 1;
      content1 = table1.getCellContents(j, row);
      content2 = table2.getCellContents(j, row);
      if (!(content1.contains("<div class=\"collapse_rim\">") && content2.contains("<div class=\"collapse_rim\">"))){
        if (content1.equals(content2))
          score += 1;
      }
      else if (content1.contains("<div class=\"collapse_rim\">") && content2.contains("<div class=\"collapse_rim\">"))
          score += 1;
    }
    score = score / (table1Cols + table2Cols);
    return score;
  }

  public boolean theTablesMatch(double score) {
    return score >= MIN_MATCH_SCORE;
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
