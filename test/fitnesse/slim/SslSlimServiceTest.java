// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import org.junit.Before;
import org.junit.Test;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.MockCommandRunner;
import fitnesse.testsystems.slim.SlimCommandRunningClient;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SslSlimServiceTest extends SlimServiceTestBase {

  protected String getImport() {
    return "fitnesse.slim.test";
  }

  protected void startSlimService() throws IOException {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{ /* "-v", */ "-ssl", "fitnesse.socketservice.SslParametersWiki", "8099"});
    SlimService.startWithFactoryAsync(JavaSlimFactory.createJavaSlimFactory(options), options);
  }

  @Before
  public void setUp() throws InterruptedException, IOException {
    createSlimService();
    slimClient = new SlimCommandRunningClient(new MockCommandRunner(new CompositeExecutionLogListener()), "localhost", 8099, 1, SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION, true, "fitnesse.socketservice.SslParametersWiki");
    statements = new ArrayList<Instruction>();
    slimClient.connect();
  }

  protected void closeSlimService() throws InterruptedException {
    SlimService.waitForServiceToStopAsync();
    assertFalse(SlimService.service.isAlive());
  }

  protected String expectedExceptionMessage() {
    return "java.lang.Exception: This is my exception";
  }

  protected String expectedStopTestExceptionMessage() {
    return "ABORT_SLIM_TEST:fitnesse.slim.test.TestSlim$StopTestException: This is a stop test exception";
  }


  @Test
  public void definedStatementSsl_returnsSslClassName() {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"-ssl", "fitnesse.socketservice.SslParametersWiki", "8099"});
    assertEquals("fitnesse.socketservice.SslParametersWiki", options.sslParameterClassName);
  }
  @Test
  public void slimClientcanIdentifyItselfAndPeer() {
	  // current setup allows only one SSL key per JVM
	  // as we run in process the SUT both names are here the same
	  // SslSlimClientBuilderTest has a test with different names in differnt processes
	  assertEquals("My Name is ", "FitNesseWiki", slimClient.getMyName());
	  assertEquals("I am connected to ", "FitNesseWiki", slimClient.getPeerName());
	  
  }
}
