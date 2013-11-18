// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SlimServiceTest extends SlimServiceTestBase {

  protected String getImport() {
    return "fitnesse.slim.test";
  }

  protected void startSlimService() throws IOException {
    SlimService.Options options = SlimService.parseCommandLine(new String[] { "8099" });
    SlimService.startWithFactoryAsync(new JavaSlimFactory(), options);
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
  public void nullInteractionService_returnsDefaultClass(){
    SlimService.Options options = SlimService.parseCommandLine(new String[] { "8099" });
    assertEquals("fitnesse.slim.fixtureInteraction.DefaultInteraction", options.interactionClass.getName());
  }

  @Test
  public void definedInteractionService_returnsCorrectClass() {
    SlimService.Options options = SlimService.parseCommandLine(new String[] { "-i", "fitnesse.slim.fixtureInteraction.InteractionDemo", "8099" });
    assertEquals("fitnesse.slim.fixtureInteraction.InteractionDemo", options.interactionClass.getName());
  }

}
