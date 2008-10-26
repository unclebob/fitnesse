package fitnesse.responders.run.slimResponder;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SlimTestSystemTest {
  private WikiPage root;
  private PageCrawler crawler;
  private FitNesseContext context;
  private MockRequest request;
  private SlimResponder responder;
  private WikiPage testPage;
  public String testResults;

  private void assertTestResultsContain(String fragment) {
    assertTrue(testResults.indexOf(fragment) != -1);
  }

  private void assertContainsReferenceToException(String string) {
    assertTrue(String.format("Should have failing style: %s", string), string.indexOf("!style_fail(") != -1);
    assertTrue(String.format("Should have reference to exception: %s", string), string.indexOf("Exception: .#") != -1);
  }

  private void getResultsForPageContents(String pageContents) throws Exception {
    request.setResource("TestPage");
    PageData data = testPage.getData();
    data.setContent(data.getContent() + "\n" + pageContents);
    testPage.commit(data);
    responder.makeResponse(context, request);
    PageData afterTest = responder.getTestResults();
    testResults = afterTest.getContent();
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    context = new FitNesseContext(root);
    request = new MockRequest();
    responder = new SlimResponder();
    responder.setFastTest(true);
    testPage = crawler.addPage(root, PathParser.parse("TestPage"), "!path classes");
  }


  @Test
  public void slimResponderStartsAndQuitsSlim() throws Exception {
    responder.setFastTest(false);
    request.setResource("TestPage");
    responder.makeResponse(context, request);
    assertTrue(!responder.slimOpen());
  }

  @Test
  public void pageHasStandardInAndOutSectionsAndCommandLine() throws Exception {
    getResultsForPageContents("");
    assertTrue(testResults.indexOf("!*> Standard Output\n\n") != -1);
    assertTrue(testResults.indexOf("!*> Standard Error\n\n") != -1);
    assertTrue(testResults.indexOf("java -cp classes fitnesse.slim.SlimService") != -1);
  }

  @Test
  public void verboseOutputIfSlimFlagSet() throws Exception {
    getResultsForPageContents("!define SLIM_FLAGS {-v}\n");
    assertTrue(testResults.indexOf("java -cp classes fitnesse.slim.SlimService -v") != -1);
  }

  @Test
  public void tableWithoutPrefixWillBeConstructed() throws Exception {
    getResultsForPageContents("|XX|\n");
    assertTestResultsContain("!-XX-! (Exception:");
    assertTestResultsContain("Class XX not found.");
  }

  @Test
  public void emptyQueryTable() throws Exception {
    getResultsForPageContents("|Query:x|\n");
    assertTestResultsContain("Query tables must have at least two rows.");
  }

  @Test
  public void queryFixtureHasNoQueryFunction() throws Exception {
    getResultsForPageContents(
      "|Query:fitnesse.slim.test.TestSlim|\n" +
        "|x|y|\n"
    );
    assertTestResultsContain("Query fixture has no valid query method");
  }

  @Test
  public void emptyScriptTable() throws Exception {
    getResultsForPageContents("|Script|\n");
    assertTestResultsContain("Script tables must at least one statement.");
  }

  @Test
  public void emptyImportTable() throws Exception {
    getResultsForPageContents("|Import|\n");
    assertTestResultsContain("Import tables must have at least two rows.");
  }

  @Test
  public void emptyTableTable() throws Exception {
    getResultsForPageContents("|Table|\n");
    assertTestResultsContain("Table tables must have at least two rows.");
  }

  @Test
  public void tableFixtureHasNoDoTableFunction() throws Exception {
    getResultsForPageContents(
      "|Table:fitnesse.slim.test.TestSlim|\n" +
        "|a|b|\n"
    );
    assertTestResultsContain("Table fixture has no valid doTable method");
  }


  @Test
  public void simpleDecisionTable() throws Exception {
    getResultsForPageContents(
      "|DT:fitnesse.slim.test.TestSlim|\n" +
        "|returnInt?|\n" +
        "|7|\n"
    );
    assertTestResultsContain("!style_pass(!-7-!)");
  }

  @Test
  public void tableWithException() throws Exception {
    getResultsForPageContents(
      "|DT:NoSuchClass|\n" +
        "|returnInt?|\n" +
        "|7|\n"
    );
    assertTestResultsContain("!anchor");
    assertTestResultsContain(".#");
    assertTestResultsContain("SlimError");
  }

  @Test
  public void tableWithBadConstructorHasException() throws Exception {
    getResultsForPageContents(
      "|DT:fitnesse.slim.test.TestSlim|badArgument|\n" +
        "|returnConstructorArgument?|\n" +
        "|3|\n"
    );
    TableScanner ts = new TableScanner(responder.getTestResults());
    Table dt = ts.getTable(0);
    assertContainsReferenceToException(dt.getCellContents(0, 0));
    assertTestResultsContain("Could not invoke constructor");
  }

  @Test
  public void tableWithBadVariableHasException() throws Exception {
    getResultsForPageContents(
      "|DT:fitnesse.slim.test.TestSlim|\n" +
        "|noSuchVar|\n" +
        "|3|\n"
    );
    TableScanner ts = new TableScanner(responder.getTestResults());
    Table table = ts.getTable(0);
    assertContainsReferenceToException(table.getCellContents(0, 2));
  }

  @Test
  public void tableWithSymbolSubstitution() throws Exception {
    getResultsForPageContents(
      "|DT:fitnesse.slim.test.TestSlim|\n" +
        "|string|getStringArg?|\n" +
        "|Bob|$V=|\n" +
        "|$V|$V|\n" +
        "|Bill|$V|\n" +
        "|John|$Q|\n"
    );
    TableScanner ts = new TableScanner(responder.getTestResults());
    Table dt = ts.getTable(0);
    assertEquals("$V<-[Bob]", dt.getCellContents(1, 2));
    assertEquals("$V->[Bob]", dt.getCellContents(0, 3));
    assertEquals("!style_pass($V->[Bob])", dt.getCellContents(1, 3));
    assertEquals("[Bill] !style_fail(expected [$V->[Bob]])", dt.getCellContents(1, 4));
    assertEquals("[John] !style_fail(expected [$Q])", dt.getCellContents(1, 5));
  }

  @Test
  public void substituteSymbolIntoExpression() throws Exception {
    getResultsForPageContents(
      "|DT:fitnesse.slim.test.TestSlim|\n" +
        "|string|getStringArg?|\n" +
        "|3|$A=|\n" +
        "|2|<$A|\n" +
        "|5|$B=|\n" +
        "|4|$A<_<$B|\n"
    );
    TableScanner ts = new TableScanner(responder.getTestResults());
    Table dt = ts.getTable(0);
    assertEquals("!style_pass(2<$A->[3])", dt.getCellContents(1, 3));
    assertEquals("!style_pass($A->[3]<4<$B->[5])", dt.getCellContents(1, 5));
  }
}
