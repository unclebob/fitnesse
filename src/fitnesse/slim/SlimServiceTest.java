package fitnesse.slim;

import static fitnesse.slim.test.StatementUtilities.list;
import fitnesse.socketservice.SocketServer;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SlimServiceTest {
  private SlimService service;
  private Socket client;
  private BufferedReader reader;
  private PrintStream writer;
  private List<Object> statements;
  private String hello;

  @Before
  public void setUp() throws Exception {
    service = new SlimService(8099);
    statements = new ArrayList<Object>();
    connect();
  }

  @After
  public void after() throws Exception {
    reader.close();
    writer.close();
    client.close();
    service.close();
  }

  private void connect() throws Exception {
    client = new Socket("localhost", 8099);
    reader = SocketServer.StreamUtility.GetBufferedReader(client);
    writer = SocketServer.StreamUtility.GetPrintStream(client);
    hello = reader.readLine();
  }

  @Test
  public void emptySession() throws Exception {
    assertTrue(hello, hello.startsWith("Slim -- V"));
  }


  @Test
  public void callOneMethod() throws Exception {
    statements.add(list("import", "fitnesse.slim.test"));
    statements.add(list("make", "testSlim", "TestSlim"));
    statements.add(list("call", "testSlim", "echoInt", "1"));
    String instructions = ListSerializer.serialize(statements);
    writer.println(instructions);
    String results = reader.readLine();
    List<Object> resultList = ListDeserializer.deserialize(results);
    assertEquals(1, resultList.size());
    assertEquals("1", resultList.get(0));
  }

  //todo test long strings  (socket might segment them)
  //todo test strings with line breaks  (readline will probably falter)
  //todo multiple instructions terminated with 'bye'.
}
