package fitnesse.slim;

import static fitnesse.util.ListUtility.list;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlimServiceTest {
  private List<Object> statements;
  private SlimClient slimClient = new SlimClient("localhost", 8099);

  @Before
  public void setUp() throws Exception {
    createSlimService();
    slimClient = new SlimClient("localhost", 8099);
    statements = new ArrayList<Object>();
    slimClient.connect();
  }

  private void createSlimService() throws Exception {
    while (!tryCreateSlimService())
      Thread.sleep(10);
  }

  private boolean tryCreateSlimService() throws Exception {
    try {
      SlimService.main(new String[] {"8099"});
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @After
  public void after() throws Exception {
    slimClient.sendBye();
    slimClient.close();
  }

  @Test
  public void emptySession() throws Exception {
    assertTrue("Connected", slimClient.isConnected());
  }

  @Test
  public void callOneMethod() throws Exception {
    addImportAndMake();
    addEchoInt("id", "1");
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("1", result.get("id"));
  }

  private void addEchoInt(String id, String number) {
    statements.add(list(id, "call", "testSlim", "echoInt", number));
  }

  private void addImportAndMake() {
    statements.add(list("i1", "import", "fitnesse.slim.test"));
    statements.add(list("m1", "make", "testSlim", "TestSlim"));
  }

  @Test
  public void makeManyCallsInOrderToTestLongSequencesOfInstructions() throws Exception {
    addImportAndMake();
    for (int i = 0; i < 1000; i++)
      addEchoInt(String.format("id_%d", i), Integer.toString(i));
    Map<String,Object> result = slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 1000; i++)
      assertEquals(i, Integer.parseInt((String) result.get(String.format("id_%d", i))));
  }

  @Test
  public void callWithLineBreakInStringArgument() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "echoString", "hello\nworld\n"));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("hello\nworld\n", result.get("id"));
  }

  @Test
  public void makeManyIndividualCalls() throws Exception {
    addImportAndMake();
    slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 100; i++) {
      statements.clear();
      addEchoInt("id", "42");
      Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
      assertEquals(1, result.size());
      assertEquals("42", result.get("id"));
    }
  }

  @Test
  public void callFunctionThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "noSuchFunction"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertExceptionWasReturned("id", results);
  }

  private void assertExceptionWasReturned(String id, Map<String, Object> results) {
    String result = (String)results.get(id);
    assertTrue(result.indexOf(SlimServer.EXCEPTION_TAG) != -1 );
  }

  @Test
  public void makeClassThatDoesntExist() throws Exception {
    statements.add(list("m1", "make","me","NoSuchClass"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertExceptionWasReturned("m1", results);
  }

  @Test
  public void useInstanceThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "noInstance", "f"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertExceptionWasReturned("id", results);
  }

  @Test
  public void verboseArgument() throws Exception {
    String args[] = {"-v", "99"};
    assertTrue(SlimService.parseCommandLine(args));
    assertTrue(SlimService.verbose);
  }


}

