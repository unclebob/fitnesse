// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.socketservice.SslParameters;
import org.junit.Before;
import org.junit.Test;

import fitnesse.socketservice.SslClientSocketFactory;
import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.MockCommandRunner;
import fitnesse.testsystems.slim.SlimCommandRunningClient;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SslSlimServiceTest extends SlimServiceTestBase {

  @Override
  protected String getImport() {
    return "fitnesse.slim.test";
  }

  @Override
  protected void startSlimService() throws IOException {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{ /* "-v", */ "-ssl", "fitnesse.socketservice.SslParametersWiki", "8099"});
    startWithFactoryAsync(JavaSlimFactory.createJavaSlimFactory(options), options);
  }

  @Override
  @Before
  public void setUp() throws InterruptedException, IOException {
    createSlimService();
    slimClient = new SlimCommandRunningClient(
      new MockCommandRunner(new CompositeExecutionLogListener()),
      "localhost", 8099, 1,
      SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION,
      new SslClientSocketFactory(SslParameters.createSslParameters("fitnesse.socketservice.SslParametersWiki")));
    statements = new ArrayList<>();
    slimClient.connect();
  }

  @Override
  protected void closeSlimService() throws InterruptedException {
    waitForServiceToStopAsync();
    assertFalse(service.isAlive());
  }

  @Override
  protected String expectedExceptionMessage() {
    return "java.lang.Exception: This is my exception";
  }

  @Override
  protected String expectedStopTestExceptionMessage() {
    return "ABORT_SLIM_TEST:fitnesse.slim.test.TestSlim$StopTestException: This is a stop test exception";
  }


  @Test
  public void definedStatementSsl_returnsSslClassName() {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"-ssl", "fitnesse.socketservice.SslParametersWiki", "8099"});
    assertEquals("fitnesse.socketservice.SslParametersWiki", options.sslParameterClassName);
  }
}
