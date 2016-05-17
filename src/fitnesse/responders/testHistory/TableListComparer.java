package fitnesse.responders.testHistory;

import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static fitnesse.responders.testHistory.HistoryComparer.MatchedPair;

public class TableListComparer {
  private HtmlTableScanner leftHandScanner;
  private HtmlTableScanner rightHandScanner;
  protected ArrayList<MatchedPair> tableMatches;


  public TableListComparer(HtmlTableScanner leftHandScanner, HtmlTableScanner rightHandScanner) {
    this.leftHandScanner = leftHandScanner;
    this.rightHandScanner = rightHandScanner;
    tableMatches = new ArrayList<>();
  }

  public void compareAllTables() {
    for (int leftTableIndex = 0; leftTableIndex < leftHandScanner.getTableCount(); leftTableIndex++) {
      for (int rightTableIndex = 0; rightTableIndex < rightHandScanner.getTableCount(); rightTableIndex++) {
        double score = compareTables(leftTableIndex, rightTableIndex);
        saveMatch(leftTableIndex, rightTableIndex, score);
      }
    }
    sortMatchesByScore();
    saveOnlyTheBestMatches();
    sortMatchesByTableIndex();
  }

  private double compareTables(int leftTableIndex, int rightTableIndex) {
    Table table1 = leftHandScanner.getTable(leftTableIndex);
    Table table2 = rightHandScanner.getTable(rightTableIndex);
    return compareTables(table1, table2);
  }

  double compareTables(Table table1, Table table2) {
    return new TableComparer(table1, table2).compareRowsOfTables();
  }

  public boolean theTablesMatch(double score) {
    return score >= HistoryComparer.MIN_MATCH_SCORE;
  }

  public void saveMatch(int leftTableIndex, int rightTableIndex, double score) {
    if (!theTablesMatch(score))
      return;
    tableMatches.add(new MatchedPair(leftTableIndex, rightTableIndex, score));
  }

  public void saveOnlyTheBestMatches() {
    for (int matchIndex = 0; matchIndex < tableMatches.size(); matchIndex++) {
      for (int secondMatchIndex = matchIndex + 1; secondMatchIndex < tableMatches.size(); secondMatchIndex++) {
        if (tableMatches.get(matchIndex).first == tableMatches.get(secondMatchIndex).first) {
          tableMatches.remove(secondMatchIndex);
          secondMatchIndex--;
        } else if (tableMatches.get(matchIndex).second == tableMatches.get(secondMatchIndex).second) {
          tableMatches.remove(secondMatchIndex);
          secondMatchIndex--;
        }
      }
    }
  }

  public void sortMatchesByScore() {
    Collections.sort(tableMatches, new Comparator<MatchedPair>() {

      @Override
      public int compare(MatchedPair match1, MatchedPair match2) {
        if (match1.matchScore > match2.matchScore)
          return -1;
        else if (match1.matchScore < match2.matchScore)
          return 1;
        else
          return 0;
      }
    });
  }

  public void sortMatchesByTableIndex() {
    Collections.sort(tableMatches, new Comparator<MatchedPair>() {

      @Override
      public int compare(MatchedPair match1, MatchedPair match2) {
        if (match1.first > match2.first)
          return 1;
        else if (match1.first < match2.first)
          return -1;
        else
          return 0;
      }
    });
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
        if (!isCalledScenario(table1, row)) {
          cellCountForTable1 += table1.getColumnCountInRow(row);
        }
        if (!isCalledScenario(table2, row)) {
          cellCountForTable2 += table2.getColumnCountInRow(row);
        }
      }
      if (cellCountForTable1 == cellCountForTable2)
        return .1;
      else
        return 0.0;
    }

    private double scoreRowContent() {
      double colScore = 0.0;
      int rowCount = 0;
      for (int row = 0; row < table1rows; row++) {
        if (!isCalledScenario(table1, row) || !isCalledScenario(table2, row)) {
          colScore += compareCellsInRow(row);
          rowCount++;
        }
      }

      return (colScore) / rowCount;
    }

    private double scoreRowTopology() {
      double score = 0.0;
      for (int row = 0; row < table1rows; row++) {
        int table1Cols = isCalledScenario(table1, row) ? 0 : table1.getColumnCountInRow(row);
        int table2Cols = isCalledScenario(table2, row) ? 0 : table2.getColumnCountInRow(row);
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
      String content1 = table1.getCellContents(col, row);
      String content2 = table2.getCellContents(col, row);
      return content1.equals(content2) ? 1 : 0;
    }

    private boolean isCalledScenario(Table table, int row) {
      if (table.getColumnCountInRow(row) == 1) {
        String content = table.getCellContents(0, row);
        return content.contains("<table>");
      }
      return false;
    }

    private boolean isCalledScenario(String content1) {
      return content1.contains("<table>");
    }
  }
}
