// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertSubString;

import java.util.ArrayList;
import java.util.List;

import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.TestSummary;
import fitnesse.util.MockSocket;
import org.junit.Before;
import org.junit.Test;
import fitnesse.util.TimeMeasurement;

public class FitClientTest implements FitClientListener {
  private List<String> outputs = new ArrayList<>();
  private List<TestSummary> counts = new ArrayList<>();
  private CommandRunningFitClient client;
  private boolean exceptionOccurred = false;

  @Before
  public void setUp() throws Exception {
    CommandRunningFitClient.TIMEOUT = 5000;
    client = new CommandRunningFitClient(new CommandRunningFitClient.OutOfProcessCommandRunner(
        new String[] { "java", "-cp", "build/classes/main", "fit.FitServer", "-v" }, null, new CompositeExecutionLogListener()));
    client.addFitClientListener(this);
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
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  @Test
  public void testOneRunUsage() throws Exception {
    doSimpleRun();
    assertFalse(exceptionOccurred);
    assertEquals(1, outputs.size());
    assertEquals(1, counts.size());
    assertSubString("class", outputs.get(0));
    assertEquals(1, counts.get(0).getRight());
  }

  private void doSimpleRun() throws Exception {
    client.start();
    Thread.sleep(100);
    client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
    client.done();
    client.join();
  }

  @Test
  public void testStandardError() throws Exception {
    client = new CommandRunningFitClient(new CommandRunningFitClient.OutOfProcessCommandRunner(new String[] { "java", "-Duser.country=US", "-Duser.language=en", "blah" }, null,
            new CompositeExecutionLogListener()));
    client.addFitClientListener(this);
    client.start();
    Thread.sleep(100);
    client.join();
    assertTrue(exceptionOccurred);
//    assertSubString("Error", client.getExecutionLog().getCapturedError());
  }

  @Test
  public void testDoesntwaitForTimeoutOnBadCommand() throws Exception {
    CommandRunningFitClient.TIMEOUT = 5000;
    TimeMeasurement measurement = new TimeMeasurement().start();
    client = new CommandRunningFitClient(new CommandRunningFitClient.OutOfProcessCommandRunner(new String[] { "java", "blah" }, null,
            new CompositeExecutionLogListener()));
    client.addFitClientListener(this);
    client.start();
    Thread.sleep(50);
    client.join();
    assertTrue(exceptionOccurred);
    assertTrue(measurement.elapsed() < CommandRunningFitClient.TIMEOUT);
  }

  @Test
  public void testOneRunWithManyTables() throws Exception {
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

  @Test
  public void testManyRuns() throws Exception {
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

  @Test
  public void testReadyForSending() throws Exception {
    CommandRunningFitClient.TIMEOUT = 5000;
    Thread startThread = new Thread() {
      @Override
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
    assertFalse(client.isSuccessfullyStarted());

    client.acceptSocket(new MockSocket(""));
    assertTrue(client.isSuccessfullyStarted());

    startThread.interrupt();
  }

  @Test
  public void testUnicodeCharacters() throws Exception {
    client.start();
    client.send("<html><table><tr><td>fitnesse.testutil.EchoFixture</td><td>\uba80\uba81\uba82\uba83</td></tr></table></html>");
    client.done();
    client.join();

    assertFalse(exceptionOccurred);
    StringBuilder buffer = new StringBuilder();
    for (String output : outputs) {
      buffer.append(output);
    }

    assertSubString("\uba80\uba81\uba82\uba83", buffer.toString());
  }
}
