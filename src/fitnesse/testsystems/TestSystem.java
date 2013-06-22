// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;

public interface TestSystem {

  void bye() throws IOException, InterruptedException;

  void kill() throws IOException;

  void runTests(TestPage pageToTest) throws IOException, InterruptedException;

  ExecutionLog getExecutionLog();

  boolean isSuccessfullyStarted();
}
