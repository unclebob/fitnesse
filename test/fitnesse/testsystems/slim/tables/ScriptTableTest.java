// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fitnesse.html.HtmlUtil;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.slim.HtmlTable;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.apache.commons.collections.ListUtils;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScriptTableTest {
  private WikiPage root;
  private List<SlimAssertion> assertions;
  public ScriptTable st;

  static class LocalizedScriptTable extends ScriptTable {

    public LocalizedScriptTable(Table table, String tableId, SlimTestContext context) {
      super(table, tableId, context);
    }

    @Override
    protected String getTableType() { return "localizedScriptTable"; }

    @Override
    protected String getTableKeyword() { return "localized script"; }

    @Override
    protected String getStartKeyword() { return "localized start"; }

    @Override
    protected String getCheckKeyword() { return "localized check"; }

    @Override
    protected String getCheckNotKeyword() { return "localized check not"; }

    @Override
    protected String getEnsureKeyword() { return "localized ensure"; }

    @Override
    protected String getRejectKeyword() { return "localized reject"; }

    @Override
    protected String getNoteKeyword() { return "localized note"; }

    @Override
    protected String getShowKeyword() { return "localized show"; }

  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    assertions = new ArrayList<>();
  }

  private ScriptTable buildInstructionsForWholeTable(String pageContents, boolean localized) throws Exception {
    st = makeScriptTable(pageContents, localized);
    assertions.addAll(st.getAssertions());
    return st;
  }

  private ScriptTable makeScriptTable(String tableText, boolean localized) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new HtmlTableScanner(root.getHtml());
    Table t = ts.getTable(0);
    SlimTestContextImpl testContext = new SlimTestContextImpl(new WikiTestPage(root));
    if (localized) return new LocalizedScriptTable(t, "id", testContext);
    else return new ScriptTable(t, "id", testContext);
  }

  private void assertScriptResults(String scriptStatements, List<List<String>> scriptResults, String table, boolean localized) throws Exception {
    buildInstructionsFor(scriptStatements, localized);
    List<List<?>> resultList = ListUtils.union(asList(asList(localized ? "localizedScriptTable_id_0" : "scriptTable_id_0", "OK")), scriptResults);
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(resultList);
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);
    assertEquals(table, HtmlUtil.unescapeWiki(st.getTable().toString()));
  }

  private void buildInstructionsFor(String scriptStatements, boolean localized) throws Exception {
    String scriptTableHeader = "|Script|\n";
    buildInstructionsForWholeTable(scriptTableHeader + scriptStatements, localized);
  }

  private List<Instruction> instructions() {
    return SlimAssertion.getInstructions(assertions);
  }

  @Test
  public void instructionsForScriptTable() throws Exception {
    buildInstructionsFor("||\n", false);
    assertEquals(0, assertions.size());
  }

  @Test
  public void startStatement() throws Exception {
    buildInstructionsFor("|start|Bob|\n", false);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedStartStatement() throws Exception {
    buildInstructionsFor("|localized start|Bob|\n", true);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void scriptWithActor() throws Exception {
    buildInstructionsForWholeTable("|script|Bob|\n", false);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedScriptWithActor() throws Exception {
    buildInstructionsForWholeTable("|localized script|Bob|\n", true);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void startStatementWithArguments() throws Exception {
    buildInstructionsFor("|start|Bob martin|x|y|\n", false);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "BobMartin", new Object[]{"x", "y"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedStartStatementWithArguments() throws Exception {
    buildInstructionsFor("|localized start|Bob martin|x|y|\n", true);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "BobMartin", new Object[]{"x", "y"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void scriptStatementWithArguments() throws Exception {
    buildInstructionsForWholeTable("|script|Bob martin|x|y|\n", false);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("scriptTable_id_0", "scriptTableActor", "BobMartin", new Object[]{"x", "y"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void scriptStatementInOneColumnWithArguments() throws Exception {
    buildInstructionsForWholeTable("|script:Bob martin|x|y|\n", false);
    List<MakeInstruction> expectedInstructions = asList(new MakeInstruction("scriptTable_id_0", "scriptTableActor", "BobMartin", new Object[]{"x", "y"}));
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedScriptStatementWithArguments() throws Exception {
    buildInstructionsForWholeTable("|localized script|Bob martin|x|y|\n", true);
    List<MakeInstruction> expectedInstructions =
      asList(
              new MakeInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "BobMartin", new Object[]{"x", "y"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void simpleFunctionCall() throws Exception {
    buildInstructionsFor("|function|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function")
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithOneArgument() throws Exception {
    buildInstructionsFor("|function|arg|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithOneArgumentAndTrailingName() throws Exception {
    buildInstructionsFor("|function|arg|trail|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "functionTrail", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void complexFunctionCallWithManyArguments() throws Exception {
    buildInstructionsFor("|eat|3|meals with|12|grams protein|3|grams fat |\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "eatMealsWithGramsProteinGramsFat", new Object[]{"3", "12", "3"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingAndOneArgument() throws Exception {
    buildInstructionsFor("|function;|arg0|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg0"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingAndMultipleArguments() throws Exception {
    buildInstructionsFor("|function;|arg0|arg1|arg2|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg0", "arg1", "arg2"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingEmbedded() throws Exception {
    buildInstructionsFor("|set name|Marisa|department and title;|QA|Tester|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "setNameDepartmentAndTitle", new Object[]{"Marisa", "QA", "Tester"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void functionCallWithSequentialArgumentProcessingEmbedded2() throws Exception {
    buildInstructionsFor("|set name|Marisa|department|QA|title and length of employment;|Tester|2 years|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "setNameDepartmentTitleAndLengthOfEmployment", new Object[]{"Marisa", "QA", "Tester", "2 years"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void checkWithFunction() throws Exception {
    buildInstructionsFor("|check|function|arg|result|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedCheckWithFunction() throws Exception {
    buildInstructionsFor("|localized check|function|arg|result|\n", true);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void checkNotWithFunction() throws Exception {
    buildInstructionsFor("|check not|function|arg|result|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedCheckNotWithFunction() throws Exception {
    buildInstructionsFor("|localized check not|function|arg|result|\n", true);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void checkWithFunctionAndTrailingName() throws Exception {
    buildInstructionsFor("|check|function|arg|trail|result|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "functionTrail", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedCheckWithFunctionAndTrailingName() throws Exception {
    buildInstructionsFor("|localized check|function|arg|trail|result|\n", true);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "functionTrail", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void rejectWithFunctionCall() throws Exception {
    buildInstructionsFor("|reject|function|arg|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedRejectWithFunctionCall() throws Exception {
    buildInstructionsFor("|localized reject|function|arg|\n", true);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void ensureWithFunctionCall() throws Exception {
    buildInstructionsFor("|ensure|function|arg|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedEnsureWithFunctionCall() throws Exception {
    buildInstructionsFor("|localized ensure|function|arg|\n", true);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void showWithFunctionCall() throws Exception {
    buildInstructionsFor("|show|function|arg|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedShowWithFunctionCall() throws Exception {
    buildInstructionsFor("|localized show|function|arg|\n", true);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("localizedScriptTable_id_0", "localizedScriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void setSymbol() throws Exception {
    buildInstructionsFor("|$V=|function|arg|\n", false);
    List<CallAndAssignInstruction> expectedInstructions =
      asList(
              new CallAndAssignInstruction("scriptTable_id_0", "V", "scriptTableActor", "function", new Object[]{"arg"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void useSymbol() throws Exception {
    buildInstructionsFor("|function|$V|\n", false);
    List<CallInstruction> expectedInstructions =
      asList(
              new CallInstruction("scriptTable_id_0", "scriptTableActor", "function", new Object[]{"$V"})
      );
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void noteDoesNothing() throws Exception {
    buildInstructionsFor("|note|blah|blah|\n", false);
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void localizedNoteDoesNothing() throws Exception {
    buildInstructionsFor("|localized note|blah|blah|\n", true);
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void initialBlankCellDoesNothing() throws Exception {
    buildInstructionsFor("||blah|blah|\n", false);
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void initialHashDoesNothing() throws Exception {
    buildInstructionsFor("|!-#comment-!|blah|blah|\n", false);
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void initialStarDoesNothing() throws Exception {
    buildInstructionsFor("|*comment|blah|blah|\n", false);
    List<Instruction> expectedInstructions = Collections.emptyList();
    assertEquals(expectedInstructions, instructions());
  }

  @Test
  public void voidActionHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
            asList(
                    asList("scriptTable_id_0", VoidConverter.VOID_TAG)
            ),
            "[[Script], [func]]", false
    );
  }

  @Test
  public void actionReturningNullHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
            asList(
                    asList("scriptTable_id_0", "null")
            ),
      "[[Script], [func]]", false
    );
  }

  @Test
  public void trueActionPasses() throws Exception {
    assertScriptResults("|func|\n",
            asList(
                    asList("scriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [pass(func)]]", false
    );
  }

  @Test
  public void falseActionFails() throws Exception {
    assertScriptResults("|func|\n",
            asList(
                    asList("scriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [fail(func)]]", false
    );
  }

  @Test
  public void checkPasses() throws Exception {
    assertScriptResults("|check|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "3")
            ),
      "[[Script], [check, func, pass(3)]]", false
    );
  }

  @Test
  public void localizedCheckPasses() throws Exception {
    assertScriptResults("|localized check|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", "3")
            ),
      "[[Script], [localized check, func, pass(3)]]", true
    );
  }

  @Test
  public void checkNotFails() throws Exception {
    assertScriptResults("|check not|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "3")
            ),
      "[[Script], [check not, func, fail(3)]]", false
    );
  }

  @Test
  public void localizedCheckNotFails() throws Exception {
    assertScriptResults("|localized check not|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", "3")
            ),
      "[[Script], [localized check not, func, fail(3)]]", true
    );
  }

  @Test
  public void checkFails() throws Exception {
    assertScriptResults("|check|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "4")
            ),
      "[[Script], [check, func, fail(a=4;e=3)]]", false
    );
  }

  @Test
  public void localizedCheckFails() throws Exception {
    assertScriptResults("|localized check|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", "4")
            ),
      "[[Script], [localized check, func, fail(a=4;e=3)]]", true
    );
  }

  @Test
  public void checkNotPasses() throws Exception {
    assertScriptResults("|check not|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "4")
            ),
      "[[Script], [check not, func, pass(a=4;e=3)]]", false
    );
  }

  @Test
  public void localizedCheckNotPasses() throws Exception {
    assertScriptResults("|localized check not|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", "4")
            ),
      "[[Script], [localized check not, func, pass(a=4;e=3)]]", true
    );
  }

  @Test
  public void ensurePasses() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
            asList(
                    asList("scriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [pass(ensure), func, 3]]", false
    );
  }

  @Test
  public void localizedEnsurePasses() throws Exception {
    assertScriptResults("|localized ensure|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [pass(localized ensure), func, 3]]", true
    );
  }

  @Test
  public void ensureFails() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
            asList(
                    asList("scriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [fail(ensure), func, 3]]", false
    );
  }

  @Test
  public void localizedEnsureFails() throws Exception {
    assertScriptResults("|localized ensure|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [fail(localized ensure), func, 3]]", true
    );
  }

  @Test
  public void rejectPasses() throws Exception {
    assertScriptResults("|reject|func|3|\n",
            asList(
                    asList("scriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [pass(reject), func, 3]]", false
    );
  }

  @Test
  public void localizedRejectPasses() throws Exception {
    assertScriptResults("|localized reject|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", BooleanConverter.FALSE)
            ),
      "[[Script], [pass(localized reject), func, 3]]", true
    );
  }

  @Test
  public void rejectFails() throws Exception {
    assertScriptResults("|reject|func|3|\n",
            asList(
                    asList("scriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [fail(reject), func, 3]]", false
    );
  }

  @Test
  public void localizedRejectFails() throws Exception {
    assertScriptResults("|localized reject|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", BooleanConverter.TRUE)
            ),
      "[[Script], [fail(localized reject), func, 3]]", true
    );
  }

  @Test
  public void show() throws Exception {
    assertScriptResults("|show|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "kawabunga")
            ),
      "[[Script], [show, func, 3, kawabunga]]", false
    );
  }

  @Test
  public void showDoesEscapes() throws Exception {
    assertScriptResults("|show|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "1 < 0")
            ),
            "[[Script], [show, func, 3, 1 < 0]]", false
    );
    assertTrue(st.getTable() instanceof HtmlTable);
    String html = ((HtmlTable) st.getTable()).toHtml();
    assertTrue(html, html.contains("1 &lt; 0"));
  }

  @Test
  public void showDoesNotEscapeValidHtml() throws Exception {
    assertScriptResults("|show|func|3|\n",
            asList(
                    asList("scriptTable_id_0", "<a href=\"http://myhost/turtle.html\">kawabunga</a>")
            ),
            "[[Script], [show, func, 3, <a href=\"http://myhost/turtle.html\">kawabunga</a>]]", false
    );
    assertTrue(st.getTable() instanceof HtmlTable);
    String html = ((HtmlTable) st.getTable()).toHtml();
    assertTrue(html.contains("<a href=\"http://myhost/turtle.html\">kawabunga</a>"));
  }

  @Test
  public void sendHtmlInstructionForTable() throws Exception {
String newLine = System.getProperty("line.separator");
    String testPage = "!define BONUSRatingTbl {| RATING_NBR | DESCR2 |\n" +
      "| 1 | Met 100% of goals |\n" +
      "| 2 | Met < 50% of goals |\n" +
            "}\n" +
            "| script |\n" +
            "| show | echo | ${BONUSRatingTbl}|\n";
    st = makeScriptTable(testPage, false);
    assertions.addAll(st.getAssertions());
    assertEquals(assertions.toString(), 2, assertions.size());
    assertEquals("Instruction{id='NOOP'}", assertions.get(0).getInstruction().toString());
    assertEquals("{id='scriptTable_id_0', instruction='call', instanceName='scriptTableActor', methodName='echo', args=[<table>" + newLine+
            "\t<tr>" + newLine +
            "\t\t<td>RATING_NBR</td>" + newLine+
            "\t\t<td>DESCR2</td>" + newLine+
            "\t</tr>" + newLine+
            "\t<tr>" + newLine+
            "\t\t<td>1</td>" + newLine+
            "\t\t<td>Met 100% of goals</td>" + newLine+
            "\t</tr>" + newLine+
            "\t<tr>" + newLine+
            "\t\t<td>2</td>" + newLine+
            "\t\t<td>Met &lt; 50% of goals</td>" + newLine+
            "\t</tr>" + newLine+
            "</table>]}", assertions.get(1).getInstruction().toString());
  }

  @Test
  public void testPlainTextWhenCellIsNotHtml() throws Exception {
    String testPage = "| script |\n" +
            "| show | echo | < 50 % |\n";
    st = makeScriptTable(testPage, false);
    assertions.addAll(st.getAssertions());
    assertEquals(assertions.toString(), 2, assertions.size());
    assertEquals("Instruction{id='NOOP'}", assertions.get(0).getInstruction().toString());
    assertEquals("{id='scriptTable_id_0', instruction='call', instanceName='scriptTableActor', methodName='echo', args=[< 50 %]}", assertions.get(1).getInstruction().toString());
  }

  @Test
  public void localizedShow() throws Exception {
    assertScriptResults("|localized show|func|3|\n",
            asList(
                    asList("localizedScriptTable_id_0", "kawabunga")
            ),
      "[[Script], [localized show, func, 3, kawabunga]]", true
    );
  }

  @Test
  public void symbolReplacement() throws Exception {
    assertScriptResults(
      "|$V=|function|\n" +
        "|check|funcion|$V|$V|\n",
            asList(
                    asList("scriptTable_id_0", "3"),
                    asList("scriptTable_id_1", "3")
            ),
      "[[Script], [$V<-[3], function], [check, funcion, $V->[3], pass($V->[3])]]", false
    );
  }

  @Test
  public void symbolReplacementAAAAAAAA() throws Exception {
    assertScriptResults(
      "|$V=|function|\n" +
       "|start|Class|$V|\n",
            asList(
                    asList("scriptTable_id_0", "3"),
                    asList("scriptTable_id_1", "OK")
            ),
      "[[Script], [$V<-[3], function], [start, pass(Class), $V->[3]]]", false
    );
  }

  @Test
  public void sameSymbolTwiceReplacement() throws Exception {
    assertScriptResults(
      "|$V=|function|\n" +
        "|check|funcion|$V $V|$V|\n",
            asList(
                    asList("scriptTable_id_0", "3"),
                    asList("scriptTable_id_1", "3")
            ),
      "[[Script], [$V<-[3], function], [check, funcion, $V->[3] $V->[3], pass($V->[3])]]", false
    );
  }

}
