//Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
//Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import fitnesse.FitNesseContext;
import fitnesse.html.HtmlUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.slim.MethodExecutionResult;
import fitnesse.testsystems.slim.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import org.junit.Before;
import org.junit.Test;

public class HtmlSlimResponderTest {
  private FitNesseContext context;
  private MockRequest request;
  protected SlimResponder responder;
  private WikiPage testPage;
  public String testResults;
  protected SimpleResponse response;
  private CustomComparatorRegistry customComparatorRegistry;

  private void assertTestResultsContain(String fragment) {
    String unescapedResults = unescape(testResults);
    assertTrue("'" + fragment + "' not found in: " +unescapedResults, unescapedResults.contains(fragment));
  }

  private void assertTestResultsDoNotContain(String fragment) {
    String unescapedResults = unescape(testResults);
    assertTrue(unescapedResults, !unescapedResults.contains(fragment));
  }

  private void getResultsForPageContents(String pageContents) throws Exception {
    request.setResource("TestPage");
    PageData data = testPage.getData();
    data.setContent(data.getContent() + "\n" + pageContents);
    testPage.commit(data);
    response = (SimpleResponse) responder.makeResponse(context, request);
    testResults = response.getContent();
  }

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    request = new MockRequest();
    customComparatorRegistry = new CustomComparatorRegistry();

    responder = getSlimResponder(customComparatorRegistry);
    responder.setFastTest(true);
    // Enforce the test runner here, to make sure we're talking to the right
    // system
    testPage = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("TestPage"),
            "!define TEST_RUNNER {fitnesse.slim.SlimService}\n!path classes");
    SlimClientBuilder.clearSlimPortOffset();
  }

  protected SlimResponder getSlimResponder(CustomComparatorRegistry customComparatorRegistry) {
    return new HtmlSlimResponder(customComparatorRegistry);
  }

  @Test
  public void tableWithoutPrefixWillBeConstructed() throws Exception {
    getResultsForPageContents("|XX|\n");
    //assertTestResultsContain("<td>XX <span class=\"error\">Could not invoke constructor for XX[0]</span> <span class=\"error\">The instance decisionTable_0.table. does not exist</span></td>");
    assertTestResultsContain("<td>XX <span class=\"error\">Could not invoke constructor for XX[0]</span></td>");
  }

  @Test
  public void emptyQueryTable() throws Exception {
    getResultsForPageContents("|Query:x|\n");
    assertTestResultsContain("Query tables must have at least two rows.");
  }

  @Test
  public void queryFixtureHasNoQueryFunction() throws Exception {
    getResultsForPageContents("!|Query:fitnesse.slim.test.TestSlim|\n"
        + "|x|y|\n");
    assertTestResultsContain(String.format(MethodExecutionResult.MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S,"query",0,"fitnesse.slim.test.TestSlim",""));
  }

  @Test
  public void emptyOrderedQueryTable() throws Exception {
    getResultsForPageContents("|ordered query:x|\n");
    assertTestResultsContain("Query tables must have at least two rows.");
  }

  @Test
  public void orderedQueryFixtureHasNoQueryFunction() throws Exception {
    getResultsForPageContents("!|ordered query:fitnesse.slim.test.TestSlim|\n"
        + "|x|y|\n");
    assertTestResultsContain(String.format(MethodExecutionResult.MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S,"query",0,"fitnesse.slim.test.TestSlim",""));
  }

  @Test
  public void emptySubsetQueryTable() throws Exception {
    getResultsForPageContents("|subset query:x|\n");
    assertTestResultsContain("Query tables must have at least two rows.");
  }

  @Test
  public void subsetQueryFixtureHasNoQueryFunction() throws Exception {
    getResultsForPageContents("!|subset query:fitnesse.slim.test.TestSlim|\n"
        + "|x|y|\n");
    assertTestResultsContain(String.format(MethodExecutionResult.MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S,"query",0,"fitnesse.slim.test.TestSlim",""));
  }

  @Test
  public void scriptTableWithBadConstructor() throws Exception {
    getResultsForPageContents("!|Script|NoSuchClass|\n");
    assertTestResultsContain("<span class=\"error\">Could not invoke constructor for NoSuchClass");
  }

  @Test
  public void emptyImportTable() throws Exception {
    getResultsForPageContents("|Import|\n");
    assertTestResultsContain("Import tables must have at least two rows.");
  }

  @Test
  public void emptyTableTable() throws Exception {
    getResultsForPageContents("!|Table:TableFixture|\n");
    assertTestResultsContain("<span class=\"error\">Could not invoke constructor for TableFixture[0]</span>");
  }

  @Test
  public void tableFixtureHasNoDoTableFunction() throws Exception {
    getResultsForPageContents("!|Table:fitnesse.slim.test.TestSlim|\n"
        + "|a|b|\n");
    assertTestResultsContain(String.format(MethodExecutionResult.MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S,"doTable",1,"fitnesse.slim.test.TestSlim",""));
  }

  @Test
  public void simpleDecisionTable() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|returnInt?|\n" + "|7|\n");
    assertTestResultsContain("<span class=\"pass\">7</span>");
  }

  @Test
  public void decisionTableIgnoresMethodMissingForResetExecuteAndTable()
      throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.DummyDecisionTable|\n"
        + "|x?|\n" + "|1|\n");
    assertEquals(0, responder.getTestSummary().getExceptions());
  }

  @Test
  public void decisionTableWithNoResetDoesNotCountExceptionsForExecute()
      throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.DummyDecisionTableWithExecuteButNoReset|\n"
        + "|x?|\n" + "|1|\n");
    assertEquals(0, responder.getTestSummary().getExceptions());
  }

  @Test
  public void queryTableWithoutTableFunctionIgnoresMissingMethodException()
      throws Exception {
    getResultsForPageContents("!|query:fitnesse.slim.test.DummyQueryTableWithNoTableMethod|\n"
        + "|x|\n" + "|1|\n");
    assertEquals(0, responder.getTestSummary().getExceptions());
  }

  @Test
  public void decisionTableWithExecuteThatThrowsDoesShowsException()
      throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.DecisionTableExecuteThrows|\n"
        + "|x?|\n" + "|1|\n");
    assertEquals(1, responder.getTestSummary().getExceptions());
    assertTestResultsContain("EXECUTE_THROWS");
  }

  @Test
  public void tableWithException() throws Exception {
    getResultsForPageContents("!|DT:NoSuchClass|\n" + "|returnInt?|\n"
        + "|7|\n");
    assertTestResultsContain("<span class=\"error\">Could not invoke constructor for NoSuchClass");
  }

  @Test
  public void tableWithBadConstructorHasException() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|badArgument|\n"
        + "|returnConstructorArgument?|\n" + "|3|\n");
    TableScanner ts = new HtmlTableScanner(testPage.getHtml());
    ts.getTable(0);
    assertTestResultsContain("Could not invoke constructor");
  }

  @Test
  public void tableWithBadVariableHasException() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|noSuchVar|\n" + "|3|\n");
    assertTestResultsContain("<span class=\"error\">" + String.format(MethodExecutionResult.MESSAGE_S_NO_METHOD_S_D_IN_CLASS_S_AVAILABLE_METHODS_S,"setNoSuchVar",1,"fitnesse.slim.test.TestSlim",""));
  }

  @Test
  public void tableWithStopTestMessageException() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|throwStopTestExceptionWithMessage?|\n" + "| once |\n"
        + "| twice |\n");
    assertTestResultsContain("<td>once <span class=\"fail\">Stop Test</span></td>");
    assertTestResultsContain("<td>twice <span class=\"ignore\">Test not run</span>");
  }

  @Test
  public void tableWithMessageException() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|throwExceptionWithMessage?|\n" + "| once |\n");
    assertTestResultsContain("<td>once <span class=\"error\">Test message</span></td>");
  }

  @Test
  public void tableWithStopTestExceptionThrown() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|throwNormal?| throwStopping? |\n"
        + "| first | second  |\n"
        + "| should fail1| true           |\n" + "\n\n"
        + "!|DT:fitnesse.slim.test.ThrowException|\n" + "|throwNormal?|\n"
        + "| should fail2|\n");
    assertTestResultsContain("<tr class=\"exception closed\">");
    assertTestResultsContain("<td> <span class=\"error\">first</span></td>");
    assertTestResultsContain("<td> <span class=\"fail\">second</span></td>");
    assertTestResultsContain("<tr class=\"exception-detail closed-detail\">");
    assertTestResultsContain("<td>should fail1 <span class=\"ignore\">Test not run</span></td>");
    assertTestResultsContain("<td>should fail2 <span class=\"ignore\">Test not run</span></td>");
  }

  @Test
  public void tableWithSymbolSubstitution() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|string|getStringArg?|\n" + "|Bob|$V=|\n" + "|$V|$V|\n");
    TableScanner ts = getScannedResults();
    Table dt = ts.getTable(0);
    assertEquals("$V<-[Bob]", unescape(dt.getCellContents(1, 2)));
    assertEquals("$V->[Bob]", unescape(dt.getCellContents(0, 3)));
  }

  protected TableScanner getScannedResults() throws Exception {
    return new HtmlTableScanner(testResults);
  }

  private String unescape(String x) {
    return HtmlUtil.unescapeWiki(HtmlUtil.unescapeHTML(x));
  }

  @Test
  public void substituteSymbolIntoExpression() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|string|getStringArg?|\n" + "|3|$A=|\n" + "|2|<$A|\n" + "|5|$B=|\n"
        + "|4|$A<_<$B|\n");
    TableScanner ts = getScannedResults();
    Table dt = ts.getTable(0);
    assertEquals("<span class=\"pass\">2<$A->[3]</span>",
        unescape(dt.getCellContents(1, 3)));
    assertEquals("<span class=\"pass\">$A->[3]<4<$B->[5]</span>",
        unescape(dt.getCellContents(1, 5)));
  }

  @Test
  public void tableWithExpression() throws Exception {
    getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n"
        + "|string|getStringArg?|\n" + "|${=3+4=}|7|\n");
    TableScanner ts = getScannedResults();
    Table dt = ts.getTable(0);
    assertEquals("<span class=\"pass\">7</span>", dt.getCellContents(1, 2));
  }

  @Test
  public void noSuchConverter() throws Exception {
    getResultsForPageContents("|!-DT:fitnesse.slim.test.TestSlim-!|\n"
        + "|noSuchConverter|noSuchConverter?|\n" + "|x|x|\n");
    assertTestResultsContain("x <span class=\"error\">No converter for fitnesse.slim.test.TestSlim$NoSuchConverter.");
  }

  @Test
  public void returnedListsBecomeStrings() throws Exception {
    getResultsForPageContents("!|script|\n"
        + "|start|fitnesse.slim.test.TestSlim|\n" + "|one list|1,2|\n"
        + "|check|get list arg|[1, 2]|\n");
    assertTestResultsContain("<td><span class=\"pass\">[1, 2]</span></td>");
  }

  @Test
  public void nullStringReturned() throws Exception {
    getResultsForPageContents("!|fitnesse.slim.test.TestSlim|\n"
        + "|nullString?|\n" + "|null|\n");
    assertTestResultsContain("<td><span class=\"pass\">null</span></td>");
  }

  @Test
  public void reportableExceptionsAreReported() throws Exception {
    getResultsForPageContents("!|fitnesse.slim.test.ExecuteThrowsReportableException|\n"
        + "|x|\n" + "|1|\n");
    assertTestResultsContain("A Reportable Exception");
  }

  @Test
  public void versionMismatchIsNotReported() throws Exception {
    getResultsForPageContents("");
    assertTestResultsDoNotContain("Slim Protocol Version Error");
  }

  @Test
  // TODO: Setting a constant here. We should use dependency inversion
  //       for the minimum require slim version to get this under test
  //       properly
  //       Had to fix this with the introduction of JUnit 4.11 since the
  //       ordering is different.
  public void versionMismatchIsReported() throws Exception {
    double oldVersionNumber = SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION;
    SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION = 1000.0; // I doubt will ever get
                                                       // here.
    try {
      getResultsForPageContents("");
      assertTestResultsContain("Slim Protocol Version Error");
    } finally {
      SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION = oldVersionNumber;
    }
  }

  @Test
  public void checkTestClassPrecededByDefine() throws Exception {
    getResultsForPageContents("!define PI {3.141592}\n" + "!path classes\n"
        + "!path fitnesse.jar\n" + "|fitnesse.testutil.PassFixture|\n");
    assertTestResultsContain("PassFixture");
  }

  @Test
  public void emptyScenarioTable() throws Exception {
    getResultsForPageContents("|Scenario|\n");
    assertTestResultsContain("Scenario tables must have a name.");
  }

  @Test
  public void scenarioTableIsRegistered() throws Exception {
    getResultsForPageContents("|Scenario|myScenario|\n");
    assertTrue("scenario should be registered", responder.testSystem.getTestContext()
        .getScenarios().iterator().next().getScenarioName().equals("myScenario"));
  }

  @Test
  public void customComparatorReturnsPass() throws Exception {
    customComparatorRegistry.addCustomComparator("equalsIgnoreCase", new EqualsIgnoreCaseComparator());
    getResultsForPageContents("!|script|\n"
        + "|start|fitnesse.slim.test.TestSlim|\n"
        + "|check|return string|equalsIgnoreCase:STRING|\n");
    assertTestResultsContain("<td><span class=\"pass\">STRING matches string</span></td>");
  }

  @Test
  public void customComparatorMultilineReturnsPass() throws Exception {
    customComparatorRegistry.addCustomComparator("equalsIgnoreCase", new EqualsIgnoreCaseComparator());
    getResultsForPageContents("!|script|\n"
        + "|start|fitnesse.slim.test.TestSlim|\n"
        + "|check|echo string|{{{!-hello\nworld-!}}}|equalsIgnoreCase:{{{!-HELLO\nWORLD-!}}}|\n");
    assertTestResultsContain("<td><span class=\"pass\">{{{HELLO\nWORLD}}} matches {{{hello\nworld}}}</span></td>");
  }

  @Test
  public void customComparatorReturnsFail() throws Exception {
    customComparatorRegistry.addCustomComparator("equalsIgnoreCase", new EqualsIgnoreCaseComparator());
    getResultsForPageContents("!|script|\n"
        + "|start|fitnesse.slim.test.TestSlim|\n"
        + "|check|return string|equalsIgnoreCase:STRINGS|\n");
    assertTestResultsContain("<td><span class=\"fail\">STRINGS doesn't match string</span></td>");
  }

  @Test
  public void customComparatorReturnsMessage() throws Exception {
    customComparatorRegistry.addCustomComparator("exceptionMessage", new ExceptionMessageComparator());
    getResultsForPageContents("!|script|\n"
        + "|start|fitnesse.slim.test.TestSlim|\n"
        + "|check|return string|exceptionMessage:STRINGS|\n");
    assertTestResultsContain("<td><span class=\"fail\">STRINGS doesn't match string:\nexception message</span></td>");
  }

  class EqualsIgnoreCaseComparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
      return expected.equalsIgnoreCase(actual);
    }
  }
	  
  class ExceptionMessageComparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
      throw new RuntimeException("exception message");
    }
  }
}
