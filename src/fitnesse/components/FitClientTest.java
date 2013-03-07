// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.fit.CommandRunningFitClient;
import fitnesse.testsystems.fit.FitSocketReceiver;
import fitnesse.testsystems.fit.SimpleSocketDoner;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.util.MockSocket;
import util.RegexTestCase;
import util.TimeMeasurement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FitClientTest extends RegexTestCase implements TestSystemListener {
  private List<String> outputs = new ArrayList<String>();
  private List<TestSummary> counts = new ArrayList<TestSummary>();
  private CommandRunningFitClient client;
  private boolean exceptionOccurred = false;
  private int port = 9080;
  private FitSocketReceiver receiver;
  private SimpleSocketDoner doner;

  public void setUp() throws Exception {
    CommandRunningFitClient.TIMEOUT = 5000;
    client = new CommandRunningFitClient(this, "java -cp classes fit.FitServer -v", port, new SocketDealer());
    receiver = new CustomFitSocketReceiver(port);
  }

  private class CustomFitSocketReceiver extends FitSocketReceiver {
    public CustomFitSocketReceiver(int port) {
      super(port, null);
    }

    protected void dealSocket(int ticket) throws Exception {
      doner = new SimpleSocketDoner(socket);
      client.acceptSocketFrom(doner);
    }
  }

  public void tearDown() throws Exception {
    receiver.close();
  }

  @Override
  public void testOutputChunk(String output) {
    outputs.add(output);
  }

  @Override
  public void testComplete(TestSummary testSummary) {
    this.counts.add(testSummary);
  }

  @Override
  public void exceptionOccurred(Throwable e) {
    exceptionOccurred = true;
    try {
      client.kill();
    }
    catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }

  public void testOneRunUsage() throws Exception {
    doSimpleRun();
    assertFalse(exceptionOccurred);
    assertEquals(1, outputs.size());
    assertEquals(1, counts.size());
    assertSubString("class", (String) outputs.get(0));
    assertEquals(1, counts.get(0).getRight());
  }

  private void doSimpleRun() throws Exception {
    receiver.receiveSocket();
    client.start();
    Thread.sleep(100);
    client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
    client.done();
    client.join();
  }

  public void testStandardError() throws Exception {
    client = new CommandRunningFitClient(this, "java blah", port, new SocketDealer());
    client.start();
    Thread.sleep(100);
    client.join();
    assertTrue(exceptionOccurred);
    assertSubString("Error", client.commandRunner.getError());
  }

  public void testDoesntwaitForTimeoutOnBadCommand() throws Exception {
    CommandRunningFitClient.TIMEOUT = 5000;
    TimeMeasurement measurement = new TimeMeasurement().start();
    client = new CommandRunningFitClient(this, "java blah", port, new SocketDealer());
    client.start();
    Thread.sleep(50);
    client.join();
    assertTrue(exceptionOccurred);
    assertTrue(measurement.elapsed() < CommandRunningFitClient.TIMEOUT);

  }

  public void testOneRunWithManyTables() throws Exception {
    receiver.receiveSocket();
    client.start();
    client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table>" +
      "<table><tr><td>fitnesse.testutil.FailFixture</td></tr></table>" +
      "<table><tr><td>fitnesse.testutil.ErrorFixture</td></tr></table></html>");
    client.done();
    client.join();
    assertFalse(exceptionOccurred);
    assertEquals(3, outputs.size());
    assertEquals(1, counts.size());
    TestSummary count = counts.get(0);
    assertEquals(1, count.getRight());
    assertEquals(1, count.getWrong());
    assertEquals(1, count.getExceptions());
  }

  public void testManyRuns() throws Exception {
    receiver.receiveSocket();
    client.start();
    client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
    client.send("<html><table><tr><td>fitnesse.testutil.FailFixture</td></tr></table></html>");
    client.send("<html><table><tr><td>fitnesse.testutil.ErrorFixture</td></tr></table></html>");
    client.done();
    client.join();

    assertFalse(exceptionOccurred);
    assertEquals(3, outputs.size());
    assertEquals(3, counts.size());
    assertEquals(1, (counts.get(0)).getRight());
    assertEquals(1, (counts.get(1)).getWrong());
    assertEquals(1, (counts.get(2)).getExceptions());
  }

  public void testDonerIsNotifiedWhenFinished_success() throws Exception {
    doSimpleRun();
    assertTrue(doner.finished);
  }

  public void testReadyForSending() throws Exception {
    CommandRunningFitClient.TIMEOUT = 5000;
    Thread startThread = new Thread() {
      public void run() {
        try {
          client.start();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    startThread.start();
    Thread.sleep(100);
    assertFalse(client.isSuccessfullyStarted());

    client.acceptSocketFrom(new SimpleSocketDoner(new MockSocket("")));
    Thread.sleep(100);
    assertTrue(client.isSuccessfullyStarted());

    startThread.interrupt();
  }

  public void testUnicodeCharacters() throws Exception {
    receiver.receiveSocket();
    client.start();
    client.send("<html><table><tr><td>fitnesse.testutil.EchoFixture</td><td>\uba80\uba81\uba82\uba83</td></tr></table></html>");
    client.done();
    client.join();

    assertFalse(exceptionOccurred);
    StringBuffer buffer = new StringBuffer();
    for (Iterator<String> iterator = outputs.iterator(); iterator.hasNext();)
      buffer.append(iterator.next());

    assertSubString("\uba80\uba81\uba82\uba83", buffer.toString());
  }
}
