// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;

public interface TestSystemListener<PageType extends TestPage> {
  void testSystemStarted(TestSystem testSystem) throws IOException;

  void testOutputChunk(String output) throws IOException;

  void testStarted(PageType testPage) throws IOException;

  void testComplete(PageType testPage, TestSummary testSummary) throws IOException;

  void testSystemStopped(TestSystem testSystem, Throwable cause /* may be null */);

  void testAssertionVerified(Assertion assertion, TestResult testResult);

  void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);
}
