// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

public interface TestSystem {

  /**
   * Get the test system name identifier, as it was provided by the user.
   * @return returns the test system name identifier
   */
  String getName();

  /**
   * Start the test system.
   */
  void start() throws UnableToStartException;

  /**
   * Close the test system. This is typically performed from the test execution thread.
   */
  void bye() throws UnableToStopException;

  /**
   * Kill the test system. This is typically invoked asynchronously.
   */
  void kill();

  /**
   * Run a collection of tests.
   * @param pageToTest TestPage to run
   */
  void runTests(TestPage pageToTest) throws TestExecutionException;

  /**
   * System is up and running.
   * @return returns boolean
   */
  boolean isSuccessfullyStarted();

  /**
   * Add a listener for test system events. During test execution the listeners will be kept
   * informed about the status of the test execution.
   * @param listener TestSystemListener
   */
  void addTestSystemListener(TestSystemListener listener);
}
