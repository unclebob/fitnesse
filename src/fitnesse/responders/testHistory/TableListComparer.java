package fitnesse.responders.testHistory;

import static fitnesse.responders.testHistory.HistoryComparer.MatchedPair;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;

import java.util.ArrayList;

public class TableListComparer {
  private HtmlTableScanner leftHandScanner;
  private HtmlTableScanner rightHandScanner;
  protected ArrayList<MatchedPair> tableMatches;


  public TableListComparer(HtmlTableScanner leftHandScanner, HtmlTableScanner rightHandScanner) {
    this.leftHandScanner = leftHandScanner;
    this.rightHandScanner = rightHandScanner;
    tableMatches = new ArrayList<MatchedPair>();
  }

  public void compareAllTables() {
    for (int leftTableIndex = 0; leftTableIndex < leftHandScanner.getTableCount(); leftTableIndex++) {
      for (int rightTableIndex = 0; rightTableIndex < rightHandScanner.getTableCount(); rightTableIndex++) {
        double score = compareTables(leftTableIndex, rightTableIndex);
        saveMatchIfBest(leftTableIndex, rightTableIndex, score);
      }
    }
  }

  private double compareTables(int leftTableIndex, int rightTableIndex) {
    Table table1 = leftHandScanner.getTable(leftTableIndex);
    Table table2 = rightHandScanner.getTable(rightTableIndex);
    return compareTables(table1, table2);
  }

  double compareTables(Table table1, Table table2) {
    return new TableComparer(table1, table2).compareRowsOfTables();
  }

  public void saveMatchIfBest(int leftTableIndex, int rightTableIndex, double score) {
    new BestMatchSaver(leftTableIndex, rightTableIndex, score).saveOnlyBestMatch();
  }

  public boolean theTablesMatch(double score) {
    return score >= HistoryComparer.MIN_MATCH_SCORE;
  }

  private class BestMatchSaver {
    private int leftHandTableIndex;
    private int rightHandTableIndex;
    private double score;
    private int rhMatch;
    private int lhMatch;
    private boolean rhMatchIsWorse;
    private boolean lhMatchIsWorse;

    public BestMatchSaver(int leftHandTableIndex, int rightHandTableIndex, double score) {
      this.leftHandTableIndex = leftHandTableIndex;
      this.rightHandTableIndex = rightHandTableIndex;
      this.score = score;
    }

    public void saveOnlyBestMatch() {
      if (!theTablesMatch(score))
        return;

      if (!findMatches())
        addNewMatch();
      else {
        determineIfMatchesAreWorse();        
        replaceAnyWorseMatchesWithBetterMatch();
      }
    }

    private void addNewMatch() {
      tableMatches.add(new MatchedPair(leftHandTableIndex, rightHandTableIndex, score));
    }

    private void determineIfMatchesAreWorse() {
      if (rhMatch != -1 && tableMatches.get(rhMatch).matchScore < score) {
        rhMatchIsWorse = true;
      }
      if (lhMatch != -1 && tableMatches.get(lhMatch).matchScore < score) {
        lhMatchIsWorse = true;
      }
    }

    private boolean findMatches() {
      rhMatch = findRightHandMatch();
      lhMatch = findLeftHandMatch();
      return lhMatch != -1 || rhMatch != -1;
    }

    private int findRightHandMatch() {
      for (int matchIndex = 0; matchIndex < tableMatches.size(); matchIndex++) {
        if (tableMatches.get(matchIndex).snd == rightHandTableIndex)
          return matchIndex;
      }
      return -1;
    }


    private int findLeftHandMatch() {
      for (int matchIndex = 0; matchIndex < tableMatches.size(); matchIndex++) {
        if (tableMatches.get(matchIndex).fst == leftHandTableIndex)
          return matchIndex;
      }
      return -1;
    }

    private void replaceAnyWorseMatchesWithBetterMatch() {
      if (thereIsAWorseLHMatchAndNoBetterRHMatch())
        replaceLHMatchWithNewBestMatchAndRemoveAnyOldRHMatch();

      if (thereIsAWorseRHMatchAndNoLHMatch())
        replaceRHMatchWithNewBestMatch();
    }

    private boolean thereIsAWorseLHMatchAndNoBetterRHMatch() {
      return lhMatchIsWorse && (rhMatch == -1 || rhMatchIsWorse);
    }

    private void replaceLHMatchWithNewBestMatchAndRemoveAnyOldRHMatch() {
      tableMatches.set(lhMatch, new MatchedPair(leftHandTableIndex, rightHandTableIndex, score));
      if (rhMatchIsWorse)
        tableMatches.remove(rhMatch);
    }

    private boolean thereIsAWorseRHMatchAndNoLHMatch() {
      return rhMatchIsWorse && lhMatch == -1;
    }

    private void replaceRHMatchWithNewBestMatch() {
      tableMatches.set(rhMatch, new MatchedPair(leftHandTableIndex, rightHandTableIndex, score));
    }
  }

  static class TableComparer {
    private Table table1;
    private Table table2;
    private int table1rows;
    private int table2rows;

    public TableComparer(Table table1, Table table2) {
      this.table1 = table1;
      this.table2 = table2;
      table1rows = table1.getRowCount();
      table2rows = table2.getRowCount();
    }

    public double compareRowsOfTables() {
      if (table1rows != table2rows)
        return 0;

      return scoreRowContent() + scoreRowTopology() + scoreTableTopology();
    }

    private double scoreTableTopology() {
      int cellCountForTable1 = 0;
      int cellCountForTable2 = 0;
      for (int row = 0; row < table1rows; row++) {
        cellCountForTable1 += table1.getColumnCountInRow(row);
        cellCountForTable2 += table2.getColumnCountInRow(row);
      }
      if (cellCountForTable1 == cellCountForTable2)
        return .1;
      else
        return 0.0;
    }

    private double scoreRowContent() {
      double colScore = 0.0;
      for (int row = 0; row < table1rows; row++)
        colScore += compareCellsInRow(row);

      return (colScore * 2) / (table1rows + table2rows);
    }

    private double scoreRowTopology() {
      double score = 0.0;
      for (int row = 0; row < table1rows; row++) {
        int table1Cols = table1.getColumnCountInRow(row);
        int table2Cols = table2.getColumnCountInRow(row);
        if (table1Cols == table2Cols)
          score += .1 * (2.0 / (table1rows + table2rows));
      }
      return score;
    }

    private double compareCellsInRow(int row) {
      double score = 0;
      int table1Cols = table1.getColumnCountInRow(row);
      int table2Cols = table2.getColumnCountInRow(row);
      int minNumberOfCols = Math.min(table1Cols, table2Cols);
      for (int col = 0; col < minNumberOfCols; col++)
        score += calculateScoreForCell(row, col);

      score = score / (table1Cols + table2Cols);
      return score;
    }

    private double calculateScoreForCell(int row, int col) {
      return scoreCellPassFailResult(row, col) + scoreCellContent(row, col);
    }

    private double scoreCellContent(int row, int col) {
      String content1 = table1.getCellContents(col, row);
      String content2 = table2.getCellContents(col, row);
      if (contentMatches(content1, content2))
        return 1;
      else
        return 0;
    }

    private boolean contentMatches(String content1, String content2) {
      return areEqualAndNotScenarioCalls(content1, content2) || bothAreScenarioCalls(content1, content2);
    }

    private boolean bothAreScenarioCalls(String content1, String content2) {
      return isCalledScenario(content1) && isCalledScenario(content2);
    }

    private boolean areEqualAndNotScenarioCalls(String content1, String content2) {
      return !isCalledScenario(content1) && !isCalledScenario(content2) && content1.equals(content2);
    }

    private double scoreCellPassFailResult(int row, int col) {
      String content1 = table1.getCellResult(col, row);
      String content2 = table2.getCellResult(col, row);
      return content1.equals(content2) ? 1 : 0;
    }

    private boolean isCalledScenario(String content1) {
      return content1.contains("<div class=\"collapse_rim\">");
    }
  }
}
