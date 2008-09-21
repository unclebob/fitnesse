package fitnesse.slim;

import static fitnesse.slim.test.StatementUtilities.list;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SlimServiceTest {
  private List<Object> statements;
  private SlimClient slimClient = new SlimClient("localhost", 8099);
  private SlimService service;

  @Before
  public void setUp() throws Exception {
    service = new SlimService(8099);
    slimClient = new SlimClient("localhost", 8099);
    statements = new ArrayList<Object>();
    slimClient.connect();
  }

  @After
  public void after() throws Exception {
    slimClient.close();
    service.close();
  }

  @Test
  public void emptySession() throws Exception {
    assertTrue("Connected", slimClient.isConnected());
  }

  @Test
  public void callOneMethod() throws Exception {
    addImportAndMake();
    addEchoInt("1");
    List<Object> resultList = slimClient.invokeAndGetResponse(statements);
    assertEquals(1, resultList.size());
    assertEquals("1", resultList.get(0));
  }

  private void addEchoInt(String number) {
    statements.add(list("call", "testSlim", "echoInt", number));
  }

  private void addImportAndMake() {
    statements.add(list("import", "fitnesse.slim.test"));
    statements.add(list("make", "testSlim", "TestSlim"));
  }

  @Test
  public void makeManyCallsInOrderToTestLongSequencesOfInstructions() throws Exception {
    addImportAndMake();
    for (int i = 0; i < 1000; i++)
      addEchoInt(Integer.toString(i));
    List<Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals(1000, result.size());
    for (int i = 0; i < 1000; i++)
      assertEquals(i, Integer.parseInt((String) result.get(i)));
  }

  @Test
  public void callWithLineBreakInStringArgument() throws Exception {
    addImportAndMake();
    statements.add(list("call", "testSlim", "echoString", "hello\nworld\n"));
    List<Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals(1, result.size());
    assertEquals("hello\nworld\n", result.get(0));
  }

  @Test
  public void makeManyIndividualCalls() throws Exception {
    addImportAndMake();
    slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 100; i++) {
      statements.clear();
      addEchoInt("42");
      List<Object> result = slimClient.invokeAndGetResponse(statements);
      assertEquals(1, result.size());
      assertEquals("42", result.get(0));
    }
  }
}

