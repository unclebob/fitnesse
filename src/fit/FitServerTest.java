// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import util.RegexTestCase;
import util.StreamReader;

public class FitServerTest extends RegexTestCase {
  private Process process;
  private Socket socket;
  private ServerSocket serverSocket;
  private InputStream socketInput;
  private OutputStream socketOutput;
  private byte[] httpRequest;
  private ByteArrayOutputStream stdoutBytes;
  private String connectionStatusSize = "0000000000";

  public void tearDown() throws Exception {
    if (process != null)
      process.destroy();
    if (socket != null)
      socket.close();
    if (serverSocket != null)
      serverSocket.close();
  }

  public void testSimpleStartUp() throws Exception {
    String junkMessage = "x";
    connectionStatusSize = "0000000001";
    prepareSessionProcess();
    assertTrue(new String(httpRequest).startsWith("GET /?responder=socketCatcher&ticket=23"));
    socketOutput.write(junkMessage.getBytes());
    process.waitFor();
  }

  public void testBadConnection() throws Exception {
    String errorMessage = "FAILURE";
    connectionStatusSize = "0000000007";
    prepareSessionProcess();
    socketOutput.write(errorMessage.getBytes());

    int exitValue = process.waitFor();
    String stdoutString = new String(stdoutBytes.toByteArray());

    assertTrue(exitValue != 0);
//TODO		This started to fail with Java 5.0... why does -1 turn into 255?
//		assertEquals("stdout: " + stdoutString, -1, exitValue);
    assertTrue(stdoutString.indexOf(errorMessage) != -1);
  }

  public void testNonTestInput() throws Exception {
    prepareSessionProcess();
    socketOutput.write("0000000020".getBytes());
    socketOutput.write("some untestable text".getBytes());
    socketOutput.flush();
    String sizeString = read(10);
    int size = Integer.parseInt(sizeString);
    String output = read(size);
    assertTrue(output.indexOf("Exception") != -1);
    assertTrue(output.indexOf("Can't find tag: table") != -1);
  }

  public void testOneSimpleRun_Fail() throws Exception {
    String table = simpleTable("FailFixture");
    prepareSessionProcess();
    checkDocumentExecution(table);
    checkDocumentResults(0, 1, 0, 0);
    terminateSessionProcess();

    assertEquals(1, process.exitValue());
  }

  public void testOneSimpleRun_Pass() throws Exception {
    String table = simpleTable("PassFixture");
    prepareSessionProcess();
    checkDocumentExecution(table);
    checkDocumentResults(1, 0, 0, 0);
    terminateSessionProcess();

    assertEquals(0, process.exitValue());
  }

  public void testTwoSimpleRuns() throws Exception {
    String table = simpleTable("FailFixture");
    prepareSessionProcess();
    checkDocumentExecution(table);
    checkDocumentResults(0, 1, 0, 0);
    checkDocumentExecution(table);
    checkDocumentResults(0, 1, 0, 0);
    terminateSessionProcess();

    assertEquals(2, process.exitValue());
  }

  public void testOneMulitiTableRun() throws Exception {
    String document = simpleTable("FailFixture") + simpleTable("FailFixture");
    prepareSessionProcess();

    FitProtocol.writeData(document, socketOutput);

    checkForTwoClassAttributesInResponse();

    checkDocumentResults(0, 2, 0, 0);
    terminateSessionProcess();
    assertEquals(2, process.exitValue());
  }

  public void testUnicodeCharacters() throws Exception {
    String table = "\uba80\uba81\uba82\uba83" + simpleTable("PassFixture");
    prepareSessionProcess();
    FitProtocol.writeData(table, socketOutput);
    String response = readWholeResponse();

    assertSubString("\uba80\uba81\uba82\uba83", response);
    terminateSessionProcess();
  }

  public void testExtraTextIdPrinted() throws Exception {
    String document = "<html>" + simpleTable("PassFixture") + "monkey" + simpleTable("PassFixture") + "</html>";
    prepareSessionProcess();

    FitProtocol.writeData(document, socketOutput);

    String response = readWholeResponse();

    assertTrue(response.startsWith("<html>"));
    assertTrue(response.indexOf("monkey") != -1);
    assertTrue(response.endsWith("</html>"));
    terminateSessionProcess();
  }

  public void testFitParseExceptionDontCrashSuite() throws Exception {
    String input = "no table";
    prepareSessionProcess();
    checkDocumentExecution(input);
    checkDocumentResults(0, 0, 0, 1);
    checkDocumentExecution(simpleTable("PassFixture"));
    checkDocumentResults(1, 0, 0, 0);
    terminateSessionProcess();

    assertEquals(1, process.exitValue());
  }

  private String read(int n) throws Exception {
    return new StreamReader(socketInput).read(n);
  }

  private void prepareSessionProcess() throws Exception {
    checkForCompetingProcesses();
    String commandWithArguments = command() + " -v localhost 1234 23";
    process = Runtime.getRuntime().exec(commandWithArguments);

    stdoutBytes = new ByteArrayOutputStream();

    watchForOutput(process.getInputStream(), new PrintStream(stdoutBytes));
    watchForOutput(process.getErrorStream(), System.err);

    establishConnection();
  }

  private void checkForCompetingProcesses() throws Exception {
    String lookupCommand = "lsof -i:1234";
    Process lookupProcess = null;
    int result = 1;
    try {
      lookupProcess = Runtime.getRuntime().exec(lookupCommand);
      result = lookupProcess.waitFor();
    } catch (IOException ex) {
      // Thrown in case where 'lsof' cannot be found. Not sure of behavior on windows.
      System.err.println("Unable to determine if another process is using port 1234 (git instaweb is a known conflict).");
      System.err.println("If any test from FitServerTest fails inexplicably make sure to eliminate competing processes.");
    }

    if (result == 0) {
      throw new IOException("Another process currently using port 1234.");
    }
  }

  private void establishConnection() throws Exception {
    serverSocket = new ServerSocket(1234);
    socket = null;

    listenForConnectionSocket();
    waitForConnectionSocket();

    assertNotNull(socket);
    assertNotNull(socketInput);
    assertNotNull(socketOutput);

    httpRequest = new byte[52]; // the precise length
    socketInput.read(httpRequest);

    socketOutput.write(connectionStatusSize.getBytes());
  }

  private void waitForConnectionSocket() throws InterruptedException {
    synchronized (serverSocket) {
      if (socket == null)
        serverSocket.wait();
    }
  }

  private void listenForConnectionSocket() {
    new Thread() {
      public void run() {
        try {
          synchronized (serverSocket) {
            socket = serverSocket.accept();
            socketInput = socket.getInputStream();
            socketOutput = socket.getOutputStream();
            serverSocket.notify();
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  private void terminateSessionProcess() throws IOException, InterruptedException {
    try {
      socketOutput.write("0000000000".getBytes());
      process.waitFor();
    } finally {
      socketInput.close();
    }
  }

  private void watchForOutput(final InputStream processOutput, final PrintStream consoleOutput) {
    new Thread() {
      public void run() {
        try {
          int b = 0;
          while ((b = processOutput.read()) != -1)
            consoleOutput.print((char) b);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  private void checkDocumentResults(int right, int wrong, int ignored, int exceptions) throws Exception {
    Counts actual = FitProtocol.readCounts(new StreamReader(socketInput));

    assertEquals(right, actual.right);
    assertEquals(wrong, actual.wrong);
    assertEquals(ignored, actual.ignores);
    assertEquals(exceptions, actual.exceptions);
  }

  private void checkDocumentExecution(String table) throws Exception {
    FitProtocol.writeData(table, socketOutput);
    checkForAttribute_class();
    checkSize("0000000000");
  }

  private void checkForAttribute_class() throws Exception {
    String output = readFromFitServer();
    assertTrue("'class' attribute was not found", output.indexOf("class=") != -1);
  }

  private String readFromFitServer() throws Exception {
    String readSize = read(10);
    int size = Integer.parseInt(readSize);
    String output = read(size);
    return output;
  }

  private void checkSize(String sizeString) throws Exception {
    assertEquals(sizeString, read(10));
  }

  private void checkForTwoClassAttributesInResponse() throws Exception {
    String response = readWholeResponse();
    int first = response.indexOf("class");
    int second = response.indexOf("class", first + 1);
    assertTrue((first >= 0) && (second > first));
  }

  private String readWholeResponse() throws Exception {
    StringBuffer buffer = new StringBuffer();
    String block = readFromFitServer();
    ;
    while (block.length() > 0) {
      buffer.append(block);
      block = readFromFitServer();
    }
    String response = buffer.toString();
    return response;
  }

  protected String command() {
    return "java -cp classes fit.FitServer";
  }

  protected String simpleTable(String fixtureName) {
    return "<table>" +
      "<tr><td>fitnesse.testutil." + fixtureName + "</td></tr>" +
      "</table>";
  }
}
