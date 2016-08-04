// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class TableTableTest {
  private WikiPage root;
  private List<SlimAssertion> assertions;
  private final String tableTableHeader =
    "|Table:fixture|argument|\n";

  public TableTable tt;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<>();
  }

  private TableTable makeTableTableAndBuildInstructions(String pageContents) throws Exception {
    tt = makeTableTable(pageContents);
    assertions.addAll(tt.getAssertions());
    return tt;
  }

  private TableTable makeTableTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(new WikiPageDummy()));
    return new TableTable(t, "id", testContext);
  }

  private void assertTableResults(String tableRows, List<List<String>> tableResults, String table) throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + tableRows);
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("tableTable_id_0", "OK"),
                    asList("tableTable_id_1", tableResults)
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals(table, tt.getTable().toString());
  }

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
  }

  @Test
  public void instructionsForEmptyTableTable() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader);
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction("tableTable_id_0", "tableTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction("tableTable_id_1", "tableTable_id", "doTable", new Object[]{asList()})
    );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void instructionsForTableTable() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + "|a|b|\n|x|y|\n");
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction("tableTable_id_0", "tableTable_id", "fixture", new Object[]{"argument"}),
            new CallInstruction("tableTable_id_1", "tableTable_id", "doTable", new Object[]{asList(asList("a", "b"), asList("x", "y"))})
    );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void oneRowThatPassesUnchanged() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("pass", "pass")
            ),
      "[[pass(Table:fixture), argument], [pass(2), pass(4)]]"
    );
  }

  @Test
  public void oneRowThatPassesChanged() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("pass:x", "pass:y")
            ),
      "[[pass(Table:fixture), argument], [pass(x), pass(y)]]"
    );
  }

  @Test
  public void oneRowThatPassesWithManyColons() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("pass:x:z", "pass:http://me")
            ),
      "[[pass(Table:fixture), argument], [pass(x:z), pass(http://me)]]"
    );
  }

  @Test
  public void oneRowThatImplicitlyFails() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("bad", "boy")
            ),
      "[[pass(Table:fixture), argument], [fail(bad), fail(boy)]]"
    );
  }

  @Test
  public void oneRowThatImplicitlyFailsWithColon() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("x:bad", "x:boy")
            ),
      "[[pass(Table:fixture), argument], [fail(x:bad), fail(x:boy)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyFails() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("fail:bad", "fail:boy")
            ),
      "[[pass(Table:fixture), argument], [fail(bad), fail(boy)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyFailsNoChange() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("fail", "fail")
            ),
      "[[pass(Table:fixture), argument], [fail(2), fail(4)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyIgnoresNoChange() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("ignore", "ignore")
            ),
      "[[pass(Table:fixture), argument], [ignore(2), ignore(4)]]"
    );
  }

  @Test
  public void oneRowThatExplicitlyIgnoresWithChange() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("ignore:x", "ignore:y")
            ),
      "[[pass(Table:fixture), argument], [ignore(x), ignore(y)]]"
    );
  }

  @Test
  public void oneRowThatReports() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("report:x", "report:y")
            ),
      "[[pass(Table:fixture), argument], [x, y]]"
    );
  }

  @Test
  public void noChange() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("no change", "no change")
            ),
      "[[pass(Table:fixture), argument], [2, 4]]"
    );
  }

  @Test
  public void blankNoChange() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("", "")
            ),
      "[[pass(Table:fixture), argument], [2, 4]]"
    );
  }

  @Test
  public void error() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("error:myError", "error:anError")
            ),
      "[[pass(Table:fixture), argument], [error(myError), error(anError)]]"
    );
  }

  @Test
  public void surplusErrors() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("", "", "error:surplus A"),
                    asList("error:surplus B", "error:surplus C")
            ),
      "[[pass(Table:fixture), argument], [2, 4, error(surplus A)], [error(surplus B), error(surplus C)]]"
    );
  }

  @Test
  public void surplusFailures() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("", "", "fail:surplus A"),
                    asList("fail:surplus B", "fail:surplus C")
            ),
      "[[pass(Table:fixture), argument], [2, 4, fail(surplus A)], [fail(surplus B), fail(surplus C)]]"
    );
  }

  @Test
  public void surplusImplicitFailures() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("", "", "fail"),
                    asList("fail", "fail")
            ),
      "[[pass(Table:fixture), argument], [2, 4, fail(fail)], [fail(fail), fail(fail)]]"
    );
  }

  @Test
  public void surplusImplicitPasses() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("", "", "pass"),
                    asList("pass", "pass")
            ),
      "[[pass(Table:fixture), argument], [2, 4, pass(pass)], [pass(pass), pass(pass)]]"
    );
  }

  @Test
  public void surplusExplicitPasses() throws Exception {
    assertTableResults("|2|4|\n",
            asList(
                    asList("", "", "pass:x"),
                    asList("pass:y", "pass:z")
            ),
      "[[pass(Table:fixture), argument], [2, 4, pass(x)], [pass(y), pass(z)]]"
    );
  }

  @Test
  public void emptyTableWithResults() throws Exception {
    assertTableResults("",
            asList(
                    asList("", "pass:x"),
                    asList("pass:y", "pass:z")
            ),
      "[[pass(Table:fixture), argument], [, pass(x)], [pass(y), pass(z)]]"
    );
  }

  @Test
  public void tableWithSymbols() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + "|$X|$X|$X|$X|\n");
    tt.setSymbol("X", "value");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
        asList(
                    asList("tableTable_id_0", "OK"),
                    asList("tableTable_id_1", asList(
                        asList("pass", "fail", "no change", "ignore")
                    ))
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals("[[pass(Table:fixture), argument], [pass($X->[value]), fail($X->[value]), $X->[value], ignore($X->[value])]]",
        tt.getTable().toString());
  }

  @Test
  public void tableWithSetSymbols() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + "|$A=|$B=|$C=|$D=|$E=|$F=|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("tableTable_id_0", "OK"),
                    asList("tableTable_id_1", asList(
                        asList("pass:1", "ignore:2", "fail:3", "sole:l", "no change", "pass")
                    ))
            )
    );

    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals("[[pass(Table:fixture), argument], [pass($A<-[1]), ignore($B<-[2]), fail($C<-[3]), fail($D<-[sole:l]), $E=, pass($F=)]]",
        tt.getTable().toString());
    assertEquals("1", tt.getSymbol("A"));
    assertEquals("2", tt.getSymbol("B"));
    assertEquals("3", tt.getSymbol("C"));
    assertEquals("sole:l", tt.getSymbol("D"));
    assertEquals(null, tt.getSymbol("E"));
    assertEquals(null, tt.getSymbol("F"));
  }

  @Test
  public void tableWithSetSymbolReturnVariableInResult() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + "|$A=|$B=|$C=|$D=|$E=|$F=|\n");
    TableTable.TableTableExpectation expectation = tt.new TableTableExpectation();
    TestResult result = expectation.evaluateExpectation(asList(asList("pass:1", "ignore:2", "fail:3", "sole:l", "no change", "pass")));

    assertNotNull(result.getVariablesToStore());
    assertEquals("1", result.getVariablesToStore().get("A"));
    assertEquals("2", result.getVariablesToStore().get("B"));
    assertEquals("3", result.getVariablesToStore().get("C"));
    assertEquals("sole:l", result.getVariablesToStore().get("D"));
    assertNull(result.getVariablesToStore().get("E"));
    assertNull(result.getVariablesToStore().get("F"));
  }

  @Test
  public void tableMethodReturnsNull() throws Exception {
    assertTableResults("|2|4|\n", null,
        "[[pass(Table:fixture), ignore(No results from table)], [2, 4]]"
      );
  }

  @Test
  public void tableMethodThrowsException() throws Exception {
    makeTableTableAndBuildInstructions(tableTableHeader + "|2|4|\n");
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList("tableTable_id_0", "OK"),
                    asList("tableTable_id_1", "Exception: except")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals("[[error(Exception: except), argument], [2, 4]]",
        tt.getTable().toString());
  }
}
