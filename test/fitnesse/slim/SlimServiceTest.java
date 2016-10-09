// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.IOException;

import fitnesse.slim.fixtureInteraction.InteractionDemo;
import org.junit.Test;

import static org.junit.Assert.*;

public class SlimServiceTest extends SlimServiceTestBase {

  @Override
  protected String getImport() {
    return "fitnesse.slim.test";
  }

  @Override
  protected void startSlimService() throws IOException {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"8099"});
    startWithFactoryAsync(JavaSlimFactory.createJavaSlimFactory(options), options);
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
  public void nullInteractionService_returnsDefaultClass() {
    String defaultInteractionName = JavaSlimFactory.createInteraction(null).getClass().getName();
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"8099"});
    assertEquals(defaultInteractionName, options.interaction.getClass().getName());
  }

  @Test
  public void definedInteractionService_returnsCorrectClass() {
    String interactionClassName = InteractionDemo.class.getName();
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"-i", interactionClassName, "8099"});
    assertEquals(interactionClassName, options.interaction.getClass().getName());
  }

  @Test
  public void undefinedStatementTimeout() {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"8099"});
    assertNull(options.statementTimeout);
  }

  @Test
  public void definedStatementTimeout_returnsTimeout() {
    SlimService.Options options = SlimService.parseCommandLine(new String[]{"-s", "1000", "8099"});
    assertEquals(1000, (int) options.statementTimeout);
  }
}
