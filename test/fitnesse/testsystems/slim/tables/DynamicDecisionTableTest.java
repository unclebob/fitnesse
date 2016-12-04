package fitnesse.testsystems.slim.tables;

import static fitnesse.slim.converters.VoidConverter.VOID_TAG;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import fitnesse.slim.instructions.CallAndAssignInstruction;
import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.testsystems.slim.SlimCommandRunningClient;

public class DynamicDecisionTableTest extends SlimTableTestSupport<DynamicDecisionTable> {
  private static final String TABLE_INSTANCE_NAME = "dynamicDecisionTable_id";
  private final String simpleDynamicDecisionTable =
    "|DDT:fixture|argument|\n" +
      "|var|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";
  private final String dynamicDecisionTableWithSameFunctionMultipleTimes= "|DDT:fixture|argument|\n" +
      "|func?|func?|\n" +
      "|3|5|\n" +
      "|7|9|\n";

  private DynamicDecisionTable dynamicDecisionTable;

  private void makeDynamicDecisionTableAndBuildInstructions(String tableText) throws Exception {
    dynamicDecisionTable = makeSlimTableAndBuildInstructions(tableText);
  }

  @Test(expected=SyntaxError.class)
  public void aDecisionTableWithOnlyTwoRowsIsBad() throws Exception {
    makeDynamicDecisionTableWithTwoRows();
  }

  private void assertTableIsBad(DynamicDecisionTable dynamicDecisionTable) {
    assertTrue(firstCellOfTable(dynamicDecisionTable).contains("Bad table"));
  }

  private String firstCellOfTable(DynamicDecisionTable dynamicDecisionTable) {
    return dynamicDecisionTable.getTable().getCellContents(0, 0);
  }

  private void makeDynamicDecisionTableWithTwoRows() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions("|DDT:x|\n|y|\n");
  }

  @Test(expected=SyntaxError.class)
  public void wrongNumberOfColumns() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(
      "|DDT:fixture|argument|\n" +
        "|var|var2|\n" +
        "|3|\n" +
        "|7|9|\n"
    );
    assertTableIsBad(dynamicDecisionTable);
  }

  @Test
  public void dynamicDecisionTableCanBeConstructorOnly() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions("|ddt:fixture|argument|\n");
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(0), TABLE_INSTANCE_NAME, "fixture", new Object[]{"argument"}),
            new CallInstruction(id(1), TABLE_INSTANCE_NAME, "table", new Object[]{asList()})
    );
    assertEquals(expectedInstructions, instructions);
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList(id(0), "OK"),
                    asList(id(1), "OK")
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dynamicDecisionTable.getTable().toString();
    String expectedColorizedTable =
      "[[pass(ddt:fixture), argument]]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void canBuildInstructionsForSimpleDecisionTable() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(simpleDynamicDecisionTable);
    int n = 0;
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(n++), TABLE_INSTANCE_NAME, "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "table", new Object[]{asList(asList("var", "func?"), asList("3", "5"), asList("7", "9"))}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "beginTable"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "3"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "7"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "endTable")

    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void canBuildInstructionsForMultipleCallsToSameSetter() throws Exception {
    String decisionTableWithSameSetterMultipleTimes = "|DDT:fixture|argument|\n" +
            "|var|var|\n" +
            "|3|5|\n" +
            "|7|9|\n";
    makeDynamicDecisionTableAndBuildInstructions(decisionTableWithSameSetterMultipleTimes);
    int n = 0;
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(n++), TABLE_INSTANCE_NAME, "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "table", new Object[]{asList(asList("var", "var"), asList("3", "5"), asList("7", "9"))}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "beginTable"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "3"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "5"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "7"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "9"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "endTable")

    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void canBuildInstructionsForMultipleCallsToSameFunction() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(dynamicDecisionTableWithSameFunctionMultipleTimes);
    int n = 0;
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(n++), TABLE_INSTANCE_NAME, "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "table", new Object[]{asList(asList("func?", "func?"), asList("3", "5"), asList("7", "9"))}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "beginTable"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "endTable")

    );
    assertEquals(expectedInstructions, instructions);
  }

  private String id(int n) {
    return TABLE_INSTANCE_NAME + "_" + n;
  }

  @Test
  public void canUseBangToCallFunction() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(
      "|DDT:fixture|argument|\n" +
      "|var|func!|\n" +
      "|3|5|\n" +
      "|7|9|\n");
    int n=0;
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(n++), TABLE_INSTANCE_NAME, "fixture", new Object[]{"argument"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "table", new Object[]{asList(asList("var", "func!"), asList("3", "5"), asList("7", "9"))}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "beginTable"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "3"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "7"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "endTable")
    );
    assertEquals(expectedInstructions, instructions);
  }


  @SuppressWarnings("unchecked")
  @Test
  public void settersAreFirstFunctionsAreLastLeftToRight() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions("|DDT:fixture|\n" +
      "|a|fa?|b|fb?|c|fc?|d|e|f|fd?|fe?|ff?|\n" +
      "|a|a|b|b|c|c|d|e|f|d|e|f|\n");
    int n = 0;

    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(n++), TABLE_INSTANCE_NAME, "fixture"),
            new CallInstruction(id(n++),TABLE_INSTANCE_NAME, "table", new Object[] {asList(
                asList("a", "fa?", "b", "fb?", "c", "fc?", "d", "e", "f", "fd?", "fe?", "ff?"),
                asList("a", "a", "b", "b", "c", "c", "d", "e", "f", "d", "e", "f"))}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "beginTable"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"a", "a"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"b", "b"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"c", "c"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"d", "d"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"e", "e"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"f", "f"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"fa"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"fb"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"fc"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"fd"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"fe"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"ff"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "endTable")
    );
    assertEquals(expectedInstructions, instructions);
  }



  @Test
  public void canBuildInstructionsForTableWithVariables() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(
      "|DDT:fixture|\n" +
        "|var|func?|\n" +
        "|3|$V=|\n" +
        "|$V|9|\n"
    );
    int n=0;
    List<Instruction> expectedInstructions = asList(
            new MakeInstruction(id(n++), TABLE_INSTANCE_NAME, "fixture"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "table", new Object[]{asList(asList("var", "func?"), asList("3", "$V="), asList("$V", "9"))}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "beginTable"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "3"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallAndAssignInstruction(id(n++), "V", TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "reset"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "set", new Object[]{"var", "$V"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "execute"),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "get", new Object[]{"func"}),
            new CallInstruction(id(n++), TABLE_INSTANCE_NAME, "endTable")
    );
    assertEquals(expectedInstructions.toString(), instructions.toString());
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTable() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(simpleDynamicDecisionTable);
    Map<String, Object> pseudoResults = makePseudoResultsForSimpleTable();
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dynamicDecisionTable.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DDT:fixture), argument], " +
        "[var, func?], " +
        "[3, pass(5)], " +
        "[7, fail(a=5;e=9)]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void translatesTestTablesIntoLiteralTables() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions("!" + simpleDynamicDecisionTable);
    Map<String, Object> pseudoResults = makePseudoResultsForSimpleTable();
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dynamicDecisionTable.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DDT:fixture), argument], " +
        "[var, func?], " +
        "[3, pass(5)], " +
        "[7, fail(a=5;e=9)]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void commentColumn() throws Exception {
    String decisionTableWithComment =
        "|DDT:fixture|argument||\n" +
          "|var|func?|#comment|\n" +
          "|3|5|comment|\n" +
          "|7|9||\n";
    makeDynamicDecisionTableAndBuildInstructions(decisionTableWithComment);
    Map<String, Object> pseudoResults = makePseudoResultsForSimpleTable();
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dynamicDecisionTable.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DDT:fixture), argument, ], " +
        "[var, func?, #comment], " +
        "[3, pass(5), comment], " +
        "[7, fail(a=5;e=9), ]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  private Map<String, Object> makePseudoResultsForSimpleTable() {
    int n = 0;
    return SlimCommandRunningClient.resultToMap(
            asList(
                    asList(id(n++), "OK"),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), VOID_TAG), //beginTable
                    asList(id(n++), VOID_TAG), //reset
                    asList(id(n++), VOID_TAG), //set
                    asList(id(n++), VOID_TAG), //execute
                    asList(id(n++), "5"),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), "5"),
                    asList(id(n++), VOID_TAG) //endTable
            )
    );
  }

  @Test
  public void canEvaluateReturnValuesAndColorizeTableForMultipleCallsToSameFunction() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(dynamicDecisionTableWithSameFunctionMultipleTimes);
    int n = 0;
    Map<String, Object> pseudoResults = SlimCommandRunningClient.resultToMap(
            asList(
                    asList(id(n++), "OK"),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), VOID_TAG), // beginTable
                    asList(id(n++), VOID_TAG), //reset
                    asList(id(n++), VOID_TAG), //execute
                    asList(id(n++), "4"),
                    asList(id(n++), "5"),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), VOID_TAG),
                    asList(id(n++), "7"),
                    asList(id(n++), "5"),
                    asList(id(n++), VOID_TAG) //endTable
            )
    );
    SlimAssertion.evaluateExpectations(assertions, pseudoResults);

    String colorizedTable = dynamicDecisionTable.getTable().toString();
    String expectedColorizedTable =
      "[" +
        "[pass(DDT:fixture), argument], " +
        "[func?, func?], " +
        "[fail(a=4;e=3), pass(5)], " +
        "[pass(7), fail(a=5;e=9)]" +
        "]";
    assertEquals(expectedColorizedTable, colorizedTable);
  }

  @Test
  public void usesDisgracedClassNames() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(
      "|DDT:slim test|\n" +
        "|x|\n" +
        "|y|\n"
    );

    Instruction makeInstruction = new MakeInstruction(id(0), TABLE_INSTANCE_NAME, "SlimTest");
    assertEquals(makeInstruction, instructions.get(0));
  }

  @Test
  public void doesNotDisgraceColumnHeadersSinceTheyAreValues() throws Exception {
    makeDynamicDecisionTableAndBuildInstructions(
      "|DDT:fixture|\n" +
        "|my var|my func?|\n" +
        "|8|7|\n"
    );
    CallInstruction setInstruction = new CallInstruction(id(4), TABLE_INSTANCE_NAME, "set", new Object[]{"my var", "8"});
    CallInstruction callInstruction = new CallInstruction(id(6), TABLE_INSTANCE_NAME, "get", new Object[]{"my func"});
    assertEquals(setInstruction, instructions.get(4));
    assertEquals(callInstruction, instructions.get(6));
  }
}
