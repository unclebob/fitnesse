package fitnesse.responders.testHistory;

import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;

import java.util.ArrayList;

public class TableComparer extends HistoryComparer {
  private HtmlTableScanner leftHandScanner;
  private HtmlTableScanner rightHandScanner;
  protected ArrayList<MatchedPair> tableMatches;
  private static Table table1;
  private static Table table2;


  public TableComparer(HtmlTableScanner leftHandScanner, HtmlTableScanner rightHandScanner) {
    this.leftHandScanner = leftHandScanner;
    this.rightHandScanner = rightHandScanner;
    tableMatches = new ArrayList<MatchedPair>();
  }

  public void compareAllTables() {
    for (int firstTableIndex = 0; firstTableIndex < leftHandScanner.getTableCount(); firstTableIndex++) {
      for (int secondTableIndex = 0; secondTableIndex < rightHandScanner.getTableCount(); secondTableIndex++) {
        double score = compareTables(leftHandScanner.getTable(firstTableIndex), rightHandScanner.getTable(secondTableIndex));
        setMatchIfItIsTheTablesBestMatch(firstTableIndex, secondTableIndex, score);
      }
    }
  }

  public void setMatchIfItIsTheTablesBestMatch(int firstTableIndex, int secondTableIndex, double score) {
    new BestMatchFinder(firstTableIndex, secondTableIndex, score).guaranteeOnlyTheBestMatchIsSaved();
  }

  public static double compareTables(Table table1, Table table2) {
    TableComparer.table1 = table1;
    TableComparer.table2 = table2;
    return compareRowsOfTables();
  }

  private static double compareRowsOfTables() {
    double score = 0;
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
      if (table1Cols == table2Cols)
        score += .1 * (2.0 / (table1rows + table2rows));
      colsScore += compareCellsInRow(row);
    }
    score += (colsScore * 2) / (table1rows + table2rows);
    if (cellCountForTable1 == cellCountForTable2)
      score += .1;
    return score;
  }

  private static double compareCellsInRow(int row) {
    int table1Cols = table1.getColumnCountInRow(row);
    int table2Cols = table2.getColumnCountInRow(row);
    double score = 0;
    int minNumberOfCols = table1Cols <= table2Cols ? table1Cols : table2Cols;
    for (int col = 0; col < minNumberOfCols; col++)
      score += calculateScoreForCell(row, col);

    score = score / (table1Cols + table2Cols);
    return score;
  }

  private static double calculateScoreForCell(int row, int col) {
    double score = 0;
    String content1 = table1.getCellResult(col, row);
    String content2 = table2.getCellResult(col, row);
    if (content1.equals(content2))
      score += 1;
    content1 = table1.getCellContents(col, row);
    content2 = table2.getCellContents(col, row);
    if (!(content1.contains("<div class=\"collapse_rim\">") && content2.contains("<div class=\"collapse_rim\">"))) {
      if (content1.equals(content2))
        score += 1;
    } else if (content1.contains("<div class=\"collapse_rim\">") && content2.contains("<div class=\"collapse_rim\">"))
      score += 1;
    return score;
  }

  public boolean theTablesMatch(double score) {
    return score >= HistoryComparer.MIN_MATCH_SCORE;
  }

  private class BestMatchFinder {
    private int leftHandTableIndex;
    private int rightHandTableIndex;
    private double score;
    private boolean foundLHMatch;
    private boolean foundRHMatch;
    private int leftHandTablesBetterMatch;
    private int rightHandTablesBetterMatch;

    public BestMatchFinder(int leftHandTableIndex, int rightHandTableIndex, double score) {
      this.leftHandTableIndex = leftHandTableIndex;
      this.rightHandTableIndex = rightHandTableIndex;
      this.score = score;
      foundLHMatch = false;
      foundRHMatch = false;
      leftHandTablesBetterMatch = -1;
      rightHandTablesBetterMatch = -1;
    }

    public void guaranteeOnlyTheBestMatchIsSaved() {
      if (!theTablesMatch(score))
        return;
      for (int matchIndex = 0; matchIndex < tableMatches.size(); matchIndex++) {
        findIfThereIsABetterLHMatch(matchIndex);
        findIfThereIsABetterRHMatch(matchIndex);
      }
      if (noMatchesWereFound())
        tableMatches.add(new MatchedPair(leftHandTableIndex, rightHandTableIndex, score));
      else
         replaceAnyOldMatchesWithBetterMatch();
    }

    private void findIfThereIsABetterRHMatch(int matchIndex) {
      if (tableMatches.get(matchIndex).snd == rightHandTableIndex) {
        foundRHMatch = true;
        if (tableMatches.get(matchIndex).matchScore < score)
          rightHandTablesBetterMatch = matchIndex;
      }
    }

    private void findIfThereIsABetterLHMatch(int matchIndex) {
      if (tableMatches.get(matchIndex).fst == leftHandTableIndex) {
        foundLHMatch = true;
        if (tableMatches.get(matchIndex).matchScore < score)
          leftHandTablesBetterMatch = matchIndex;
      }
    }

    private boolean noMatchesWereFound() {
      return !foundLHMatch && !foundRHMatch;
    }

    private void replaceAnyOldMatchesWithBetterMatch() {
      if (thereIsAWorseLHMatchAndNoBetterRHMatch())
          replaceLHMatchWithNewBestMatchAndRemoveAnyOldRHMatch();

      if (thereIsAWorseRHMatchAndNoLHMatch())
          replaceRHMatchWithNewBestMatch();
    }

    private boolean thereIsAWorseLHMatchAndNoBetterRHMatch() {
      return (foundLHMatch && leftHandTablesBetterMatch != -1)&&(!foundRHMatch || rightHandTablesBetterMatch != -1);
    }

    private void replaceLHMatchWithNewBestMatchAndRemoveAnyOldRHMatch() {
      tableMatches.set(leftHandTablesBetterMatch, new MatchedPair(leftHandTableIndex, rightHandTableIndex, score));
      if (rightHandTablesBetterMatch != -1)
        tableMatches.remove(rightHandTablesBetterMatch);
    }

    private boolean thereIsAWorseRHMatchAndNoLHMatch() {
      return (foundRHMatch && rightHandTablesBetterMatch != -1)&&(!foundLHMatch);
    }

    private void replaceRHMatchWithNewBestMatch() {
      tableMatches.set(rightHandTablesBetterMatch, new MatchedPair(leftHandTableIndex, rightHandTableIndex, score));
    }
  }
}
