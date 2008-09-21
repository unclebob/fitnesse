package fitnesse.slim;

import static fitnesse.slim.test.StatementUtilities.list;
import static fitnesse.slim.test.StatementUtilities.statement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ListExecutorTest {
  private List<Object> statements;
  private ListExecutor executor;

  @Before
  public void setup() {
    executor = new ListExecutor();
    statements = new ArrayList<Object>();
    statements.add(statement("import", "fitnesse.slim.test"));
    statements.add(statement("make", "testSlim", "TestSlim"));
  }

  private void respondsWith(List<Object> expected) {
    List<Object> result = executor.execute(statements);
    assertEquals(expected, result);
  }

  @Test(expected = SlimError.class)
  public void invalidOperation() throws Exception {
    statements.add(statement("invalidOperation"));
    respondsWith(list("shouldn't get here"));
  }

  @Test(expected = SlimError.class)
  public void malformedStatement() throws Exception {
    statements.add(statement("call", "notEnoughArguments"));
    respondsWith(list("shouldn't get here"));
  }

  @Test(expected = SlimError.class)
  public void noSuchInstance() throws Exception {
    statements.add(statement("call", "noSuchInstance", "noSuchMethod"));
    respondsWith(list("Shouldn't get here"));
  }

  @Test
  public void emptyListReturnsNicely() throws Exception {
    statements.clear();
    executor.execute(statements);
    respondsWith(list());
  }

  @Test
  public void createWithFullyQualifiedNameWorks() throws Exception {
    statements.clear();
    statements.add(statement("make", "testSlim", "fitnesse.slim.test.TestSlim"));
    respondsWith(list());
  }

  @Test
  public void oneFunctionCall() throws Exception {
    statements.add(statement("call", "testSlim", "returnString"));
    respondsWith(list("string"));
  }

  @Test
  public void multiFunctionCall() throws Exception {
    statements.add(statement("call", "testSlim", "add", "1", "2"));
    statements.add(statement("call", "testSlim", "add", "3", "4"));
    respondsWith(list("3", "7"));
  }

  @Test
  public void callAndAssign() throws Exception {
    statements.add(statement("callAndAssign", "v", "testSlim", "add", "5", "6"));
    statements.add(statement("call", "testSlim", "echoInt", "$v"));
    respondsWith(list("11", "11"));
  }

  @Test
  public void describeClass() throws Exception {
    statements.add(statement("describeClass", "Describable"));
    List<Object> results = executor.execute(statements);
    List<Object> description = (List<Object>) results.get(0);
    List<String> variables = (List<String>) description.get(0);
    List<String> methods = (List<String>) description.get(1);
    assertListHas(variables, "variable");
    assertListHas(variables, "baseVariable");
    assertListHas(methods, "baseMethod(0)");
    assertListHas(methods, "method(2)");
  }

  private void assertListHas(List<String> variables, String value) {
    assertTrue(variables.indexOf(value) != -1);
  }

  @Test
  public void getAndSetVariables() throws Exception {
    statements.add(statement("make", "d", "Describable"));
    statements.add(statement("set", "d", "variable", "1"));
    statements.add(statement("get", "d", "variable"));
    respondsWith(list("1"));
  }


}
