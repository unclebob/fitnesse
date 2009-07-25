package fitnesse.responders.testHistory;

import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.htmlparser.util.ParserException;

public class TableListComparerTest {
  private TableListComparer comparer;

  @Before
  public void setUp() throws ParserException {
    HtmlTableScanner leftHandScanner = new HtmlTableScanner("<table>empty</table>");
    HtmlTableScanner rightHandScanner = new HtmlTableScanner("<table>empty</table>");
    comparer = new TableListComparer(leftHandScanner,rightHandScanner);
  }


  @Test
  public void shouldOnlyUseTheBestMatchForTheFirstTable() throws Exception {
    comparer.saveMatch(1, 1, 1.0);
    comparer.saveMatch(1,2,1.1);
    comparer.sortMatchesByScore();
    comparer.saveOnlyTheBestMatches();
    assertEquals(1.1, comparer.tableMatches.get(0).matchScore, .01);
  }

  @Test
  public void shouldOnlyReplaceAMatchIfThereIsNoBetterMatchForEitherTable() throws Exception {
    comparer.saveMatch(1, 1, 1.0);
    comparer.saveMatch(3, 2, 1.2);
    comparer.saveMatch(1,2,1.1);
    comparer.sortMatchesByScore();
    comparer.saveOnlyTheBestMatches();
    assertEquals(1.2, comparer.tableMatches.get(0).matchScore, .001);
    assertEquals(1.0, comparer.tableMatches.get(1).matchScore, .001);
    assertEquals(2, comparer.tableMatches.size());
  }

  @Test
  public void shouldRemoveOldMatchesIfBetterOnesAreFound() throws Exception {
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(1, 1, 1.0));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(3, 2, 1.0));
    comparer.saveMatch(1,2,1.1);
    comparer.sortMatchesByScore();
    comparer.saveOnlyTheBestMatches();
    assertEquals(1.1, comparer.tableMatches.get(0).matchScore, .001);
    assertEquals(1, comparer.tableMatches.size());
  }

  @Test
  public void shouldReplaceOldMatchForSecondTableEvenIfThereIsNoMatchForFirstTable() throws Exception {
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(3, 2, 1.0));
    comparer.saveMatch(1,2,1.1);
    comparer.sortMatchesByScore();
    comparer.saveOnlyTheBestMatches();
    assertEquals(1.1, comparer.tableMatches.get(0).matchScore, .001);
    assertEquals(1, comparer.tableMatches.size());
  }

    @Test
  public void shouldGetAScoreBackFromCompareTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>x</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    double score = comparer.compareTables(table1,table2);
    assertEquals(HistoryComparer.MAX_MATCH_SCORE,score, .01);
  }

  @Test
  public void shouldCompareTwoSimpleEqualTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>x</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    assertTrue(comparer.theTablesMatch(comparer.compareTables(table1, table2)));
  }

  @Test
  public void shouldCompareTwoSimpleUnequalTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>y</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    assertFalse(comparer.theTablesMatch(comparer.compareTables(table1, table2)));
  }

  @Test
  public void shouldCompareTwoDifferentlySizedTables() throws Exception {
    String table1text = "<table><tr><td>x</td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    String table2text = "<table><tr><td>x</td><td>y</td></tr></table>";
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    assertFalse(comparer.theTablesMatch(comparer.compareTables(table1, table2)));
  }

    @Test
  public void shouldIgnoreCollapsedTables() throws Exception {
    String table1text = "<table><tr><td>has collapsed table</td><td><div class=\"collapse_rim\"> <tr><td>bleh1</td></tr></div></td></tr></table>";
    String table2text = "<table><tr><td>has collapsed table</td><td><div class=\"collapse_rim\"> <tr><td>HAHA</td></tr></div></td></tr></table>";
    Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
    Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
    double score = comparer.compareTables(table1, table2);
    assertEquals(HistoryComparer.MAX_MATCH_SCORE,score,.01 );
    assertTrue(comparer.theTablesMatch(score));
  }

  @Test
   public void shouldCheckTheMatchScoreToSeeIfTablesMatch() throws Exception {
     double score = 1.0;
     assertTrue(comparer.theTablesMatch(score));
     score = .79;
     assertFalse(comparer.theTablesMatch(score));
     score = 1.1;
     assertTrue(comparer.theTablesMatch(score));
   }

  @Test
  public void shouldKeepTheBestScoreForATableEvenIfItIsHasABetterMatchItCantKeep() throws Exception {
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(6, 6, 1.0));
    comparer.saveMatch(6,7,1.1);
    comparer.saveMatch(7,7,1.2);
    comparer.sortMatchesByScore();
    comparer.saveOnlyTheBestMatches();
    assertEquals(1.2, comparer.tableMatches.get(0).matchScore, .001);
    assertEquals(1.0, comparer.tableMatches.get(1).matchScore, .001);
    assertEquals(2, comparer.tableMatches.size());
  }

  @Test
  public void shouldBeAbleToOrderTheMatchesHighestToLowest() throws Exception {
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(6, 6, 1.0));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(5, 5, .9));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(4, 4, 1.1));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(7, 7, 1.05));
    comparer.sortMatchesByScore();
    assertEquals(1.1, comparer.tableMatches.get(0).matchScore, .001);
    assertEquals(1.05, comparer.tableMatches.get(1).matchScore, .001);
    assertEquals(1.0, comparer.tableMatches.get(2).matchScore, .001);
    assertEquals(.9, comparer.tableMatches.get(3).matchScore, .001);
  }

  @Test
  public void shouldBeAbleToOrderTheMatchesByTableIndex() throws Exception {
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(6, 6, 1.0));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(5, 5, .9));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(4, 4, 1.1));
    comparer.tableMatches.add(new HistoryComparer.MatchedPair(7, 7, 1.05));
    comparer.sortMatchesByTableIndex();
    assertEquals(4, comparer.tableMatches.get(0).first);

  }

}
