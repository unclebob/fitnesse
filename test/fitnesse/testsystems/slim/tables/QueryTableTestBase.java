package fitnesse.testsystems.slim.tables;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.html.HtmlUtil;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public abstract class QueryTableTestBase {
  private WikiPage root;
  private List<SlimAssertion> assertions;
  private String queryTableHeader;
  private QueryTable qt;
  private SlimTestContextImpl testContext;
  protected String headRow;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<>();
    queryTableHeader =
      "|" + tableType() + ":fixture|argument|\n" +
        "|n|2n|\n";
    headRow = "[pass(" + tableType() + ":fixture), argument], ";
  }

  protected abstract String tableType();

  protected QueryTable makeQueryTableAndBuildInstructions(String pageContents) throws Exception {
    qt = makeQueryTable(pageContents);
    assertions.addAll(qt.getAssertions());
    return qt;
  }

  private QueryTable makeQueryTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    testContext = new SlimTestContextImpl(new WikiTestPage(root));
    return constructQueryTable(t);
  }

  private QueryTable constructQueryTable(Table t) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
    return (QueryTable) new SlimTableFactory().makeSlimTable(t, "id", testContext);
  }

  protected void assertQueryResults(String queryRows, List<List<List<String>>> queryResults, String table) throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + queryRows);
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(asList(asList("queryTable_id_0", "OK"), asList("queryTable_id_1", "blah"), asList("queryTable_id_2", queryResults)));
    evaluateResults(pseudoResults, table);
  }

  @Test
  public void instructionsForQueryTable() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader);
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction("queryTable_id_0", "queryTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction("queryTable_id_1", "queryTable_id", "table", new Object[]{asList(asList("n", "2n"))}),
            new CallInstruction("queryTable_id_2", "queryTable_id", "query"));
    assertEquals(expectedInstructions, instructions());
  }

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
  }

  @Test
  public void nullResultsForNullTable() throws Exception {
    assertQueryResults("", new ArrayList<List<List<String>>>(),
      "[" +
        headRow +
        "[n, 2n]" +
        "]"
    );
  }

  @Test
  public void oneRowThatMatches() throws Exception {
    assertQueryResults("|2|4|\n",
            asList(
                    asList(
                            asList("n", "2"),
                            asList("2n", "4"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), pass(4)]" +
        "]"
    );
  }

  @Test
  public void oneRowFirstCellMatchesSecondCellBlank() throws Exception {
    assertQueryResults("|2||\n",
             asList(
                     asList(
                             asList("n", "2"),
                             asList("2n", "4"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), ignore(4)]" +
        "]"
    );
  }

  @Test
  public void oneRowThatFails() throws Exception {
    assertQueryResults("|2|4|\n",
             asList(
                     asList(
                             asList("n", "3"),
                             asList("2n", "5"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[fail(e=2;missing), 4], " +
        "[fail(a=3;surplus), 5]" +
        "]"
    );
  }

  @Test
  public void oneRowWithPartialMatch() throws Exception {
    assertQueryResults("|2|4|\n",
             asList(
                     asList(
                             asList("n", "2"),
                             asList("2n", "5"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), fail(a=5;e=4)]" +
        "]"
    );
  }

  @Test
  public void twoMatchingRows() throws Exception {
    assertQueryResults(
      "|2|4|\n" +
        "|3|6|\n",
             asList(
                     asList(
                             asList("n", "2"),
                             asList("2n", "4")),
                     asList(
                             asList("n", "3"),
                             asList("2n", "6"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2), pass(4)], " +
        "[pass(3), pass(6)]" +
        "]"
    );
  }

  @Test
  public void twoRowsFirstMatchesSecondDoesnt() throws Exception {
    assertQueryResults(
      "|3|6|\n" +
        "|99|99|\n",
             asList(
                     asList(
                             asList("n", "2"),
                             asList("2n", "4")),
                     asList(
                             asList("n", "3"),
                             asList("2n", "6"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), pass(6)], " +
        "[fail(e=99;missing), 99], " +
        "[fail(a=2;surplus), 4]" +
        "]"
    );
  }

  @Test
  public void twoRowsSecondMatchesFirstDoesnt() throws Exception {
    assertQueryResults(
      "|99|99|\n" +
        "|2|4|\n",
             asList(
                     asList(
                             asList("n", "2"),
                             asList("2n", "4")),
                     asList(
                             asList("n", "3"),
                             asList("2n", "6"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[fail(e=99;missing), 99], " +
        "[pass(2), pass(4)], " +
        "[fail(a=3;surplus), 6]" +
        "]"
    );
  }

  @Test
  public void fieldInMatchingRowDoesntExist() throws Exception {
    assertQueryResults(
      "|3|4|\n",
             asList(asList(asList("n", "3"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(3), fail(a=field 2n not present;e=4)]" +
        "]"
    );
    assertEquals(1, testContext.getTestSummary().getRight());
    assertEquals(1, testContext.getTestSummary().getWrong());
  }

  @Test
  public void fieldInSurplusRowDoesntExist() throws Exception {
    assertQueryResults(
      "",
             asList(asList(asList("n", "3"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[fail(a=3;surplus), fail(field 2n not present)]" +
        "]"
    );
    assertEquals(0, testContext.getTestSummary().getRight());
    assertEquals(2, testContext.getTestSummary().getWrong());
  }

  @Test
  public void variablesAreReplacedInMatch() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|2|$V|\n");
    qt.setSymbol("V", "4");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", VoidConverter.VOID_TAG),
                    asList("queryTable_id_2", asList(
                            asList(
                                    asList("n", "2"),
                                    asList("2n", "4")))))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals(
            "[" +
                    headRow +
                    "[n, 2n], " +
                    "[pass(2), pass($V->[4])]" +
                    "]",
            HtmlUtil.unescapeWiki(qt.getTable().toString())
    );
  }

  @Test
  public void tableWithSetSymbol() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|1|$A=|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
        asList(
            asList("queryTable_id_0", "OK"),
            asList("queryTable_id_2", asList(asList(asList("n", "1"), asList("2n", "2"))))
        )
    );
    evaluateResults(pseudoResults, "[" +
        headRow +
        "[n, 2n], " +
        "[pass(1), ignore($A<-[2])]" + "]");

    assertEquals("2", qt.getSymbol("A"));
  }

  @Test
  public void tableWithSetSymbolReturnVariableInResult() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|1|$A=|\n");
    QueryTable.QueryTableExpectation expectation = qt.new QueryTableExpectation();
    TestResult result = expectation.evaluateExpectation(asList(asList(asList("n", "1"), asList("2n", "2"))));

    assertNotNull(result.getVariablesToStore());
    assertEquals("2", result.getVariablesToStore().get("A"));
  }

  @Test
  public void tableWithSetSymbolInFirstColumnReturnVariableInResult() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|$A=|2|\n");
    QueryTable.QueryTableExpectation expectation = qt.new QueryTableExpectation();
    TestResult result = expectation.evaluateExpectation(asList(asList(asList("n", "1"), asList("2n", "2"))));

    assertNotNull(result.getVariablesToStore());
    assertEquals("1", result.getVariablesToStore().get("A"));
  }


  @Test
  public void commentColumn() throws Exception {
	queryTableHeader =
			      "|" + tableType() + ":fixture|argument|\n" +
			        "|#comment1|n|#comment2|\n";


    assertQueryResults(
		      "|first|1|comment|\n"+
		      "|second|2||\n"		  ,
             asList(
                     asList(
                             asList("#comment1", "second"),
                             asList("n", "1"),
                             asList("#comment2", "")),
                     asList(
                             asList("#comment1", "first"),
                             asList("n", "2"),
                             asList("#comment2", "comment"))),
		      "[" +
		        headRow +
		        "[#comment1, n, #comment2], " +
		        "[first, pass(1), comment], " +
		        "[second, pass(2), ]" +
		        "]"
		    );

  }

  @Test
  public void variablesAreReplacedInExpected() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|2|$V|\n");
    qt.setSymbol("V", "5");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", VoidConverter.VOID_TAG),
                    asList("queryTable_id_2",
                            asList(
                                    asList(
                                            asList("n", "2"),
                                            asList("2n", "4")))))
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals(
            "[" +
                    headRow +
                    "[n, 2n], " +
                    "[pass(2), fail(a=4;e=$V->[5])]" +
                    "]",
            HtmlUtil.unescapeWiki(qt.getTable().toString())
    );
  }

  @Test
  public void variablesAreReplacedInMissing() throws Exception {
    makeQueryTableAndBuildInstructions(queryTableHeader + "|3|$V|\n");
    qt.setSymbol("V", "5");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", VoidConverter.VOID_TAG),
                    asList("queryTable_id_2", new ArrayList<>()))
    );
    evaluateResults(pseudoResults, "[" +
      headRow +
      "[n, 2n], " +
      "[fail(e=3;missing), $V->[5]]" +
      "]");
  }

  protected void evaluateResults(Map<String, Object> pseudoResults, String expectedTable) {
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals(expectedTable, qt.getTable().toString());
  }

  @Test
  public void oneRowThatMatchesExpression() throws Exception {
    assertQueryResults("|<5|4|\n",
             asList(
                     asList(
                             asList("n", "2"),
                             asList("2n", "4"))),
      "[" +
        headRow +
        "[n, 2n], " +
        "[pass(2<5), pass(4)]" +
        "]"
    );
  }

  @Test
  public void anErrorShouldBeRegisteredIfQueryDoesNotReturnAList() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ": a|\n|b|\n");
    QueryTable.QueryTableExpectation expectation = qt.new QueryTableExpectation();
    TestResult result = expectation.evaluateExpectation("String result");

    assertEquals(ExecutionResult.ERROR, result.getExecutionResult());
    assertEquals(1, testContext.getTestSummary().getExceptions());
  }

  @Test
  public void ShouldBeIgnoredIfQueryResultIsNull() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ": a|\n|b|\n");
    QueryTable.QueryTableExpectation expectation = qt.new QueryTableExpectation();
    TestResult result = expectation.evaluateExpectation(null);

    assertEquals(ExecutionResult.IGNORE, result.getExecutionResult());
  }

  /* When one row has a higher score "from the right", it is still the row that is correct from the left that should match. */
  @Test
  public void shouldMatchQueryFromLeftToRight() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ":fixture|argument|\n" +
            "|x|n|2n|\n" +
            "|1|2|4|\n" +
            "|2|3|6|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", "blah"),
                    asList("queryTable_id_2",
                            asList(
                                    asList(
                                            asList("x", "1"),
                                            asList("n", "3"),
                                            asList("2n", "6"))))));
    evaluateResults(pseudoResults, "[" +
            headRow +
            "[x, n, 2n], " +
            "[pass(1), fail(a=3;e=2), fail(a=6;e=4)], " +
            "[fail(e=2;missing), 3, 6]" +
            "]");
  }

  @Test
  public void shouldMatchQueryFromLeftToRightWithFirstCellEmpty() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ":fixture|argument|\n" +
            "|x|n|2n|\n" +
            "||2|4|\n" +
            "||3|6|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", "blah"),
                    asList("queryTable_id_2",
                            asList(
                                    asList(
                                            asList("x", "1"),
                                            asList("n", "2"),
                                            asList("2n", "4")),
                                    asList(
                                            asList("x", "1"),
                                            asList("n", "3"),
                                            asList("2n", "6"))))));
    evaluateResults(pseudoResults, "[" +
            headRow +
            "[x, n, 2n], " +
            "[ignore(1), pass(2), pass(4)], " +
            "[ignore(1), pass(3), pass(6)]" +
            "]");
  }

  @Test
  public void shouldMatchQueryFromLeftToRightWithSecondCellEmpty() throws Exception {
    makeQueryTableAndBuildInstructions("|" + tableType() + ":fixture|argument|\n" +
            "|x|n|2n|\n" +
            "|1||4|\n" +
            "|1||6|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("queryTable_id_0", "OK"),
                    asList("queryTable_id_1", "blah"),
                    asList("queryTable_id_2",
                            asList(
                                    asList(
                                            asList("x", "1"),
                                            asList("n", "2"),
                                            asList("2n", "4")),
                                    asList(
                                            asList("x", "1"),
                                            asList("n", "3"),
                                            asList("2n", "6"))))));
    evaluateResults(pseudoResults, "[" +
            headRow +
            "[x, n, 2n], " +
            "[pass(1), ignore(2), pass(4)], " +
            "[pass(1), ignore(3), pass(6)]" +
            "]");
  }
}
