// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

public interface TestSystemListener {
  default void testSystemStarted(TestSystem testSystem) {
  }

  /**
   * Appends content to test output.
   * This method only exists to provide forward compatibility while Listeners are migrated to the overload also accepting a TestPage.
   * @param output content to append
   * @deprecated implement {@link #testOutputChunk(TestPage, String)}
   */
  @Deprecated
  default void testOutputChunk(String output) {
    throw new UnsupportedOperationException("This overload is deprecated, and should not be called any more. " +
      "It only exists to provide forward compatibility while Listeners are migrated to the overload also accepting a TestPage.");
  }

  /**
   * Appends content to test output.
   * This method only has a default implementation to provide backwards compatibility for Listeners not yet migrated to implement it.
   * All TestSystemListeners should implement it.
   * @param testPage current test page
   * @param output content to append
   */
  default void testOutputChunk(TestPage testPage, String output) {
    testOutputChunk(output);
  }

  default void testStarted(TestPage testPage) {
  }

  default void testComplete(TestPage testPage, TestSummary testSummary) {
  }

  default void testSystemStopped(TestSystem testSystem, Throwable cause /* may be null */) {
  }

  default void testAssertionVerified(Assertion assertion, TestResult testResult) {
  }

  default void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
  }
}
