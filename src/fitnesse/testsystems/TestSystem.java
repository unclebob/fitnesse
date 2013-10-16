// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;

public interface TestSystem {

  /**
   * Get the test system name identifier, as it was provided by the user.
   * @return
   */
  String getName();

  /**
   * Start the test system.
   * @throws IOException
   */
  void start() throws IOException;

  /**
   * Close the test system. This is typically performed from the test execution thread.
   * @throws IOException
   * @throws InterruptedException
   */
  void bye() throws IOException, InterruptedException;

  /**
   * Kill the test system. This is typically invoked asynchronously.
   * @throws IOException
   */
  void kill() throws IOException;

  /**
   * Run a collection of tests.
   * @param pageToTest
   * @throws IOException
   * @throws InterruptedException
   */
  void runTests(TestPage pageToTest) throws IOException, InterruptedException;

  /**
   * System is up and running.
   * @return
   */
  boolean isSuccessfullyStarted();

  /**
   * Add a listener. During test execution the listeners will be kept informed about the
   * status of the test execution.
   * @param listener
   */
  void addTestSystemListener(TestSystemListener listener);
}
