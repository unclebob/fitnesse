// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class SlimServiceTest extends SlimServiceTestBase {

  protected String getImport() {
    return "fitnesse.slim.test";
  }

  protected void startSlimService() throws Exception {
    SlimService.main(new String[] { "8099" });
  }

  protected String expectedExceptionMessage() {
    return "java.lang.Exception: This is my exception";
  }

  protected String expectedStopTestExceptionMessage() {
    return "ABORT_SLIM_TEST:fitnesse.slim.test.TestSlim$StopTestException: This is a stop test exception";
  }


  @Test
  public void nullInteractionService_returnsDefaultClass(){
    SlimService.interactionClassName = null;
    assertEquals("fitnesse.slim.fixtureInteraction.DefaultInteraction", SlimService.getInteractionClass().getName());
  }

  @Test
  public void definedInteractionService_returnsCorrectClass() {
    SlimService.interactionClassName = "fitnesse.slim.fixtureInteraction.InteractionDemo";
    assertEquals("fitnesse.slim.fixtureInteraction.InteractionDemo", SlimService.getInteractionClass().getName());
  }

}
