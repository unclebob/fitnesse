// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

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

}
