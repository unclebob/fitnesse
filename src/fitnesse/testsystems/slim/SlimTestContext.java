// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.Collection;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.tables.ScenarioTable;

public interface SlimTestContext {
  String getSymbol(String symbolName);

  void setSymbol(String symbolName, String value);

  void addScenario(String scenarioName, ScenarioTable scenarioTable);

  ScenarioTable getScenario(String scenarioName);

  ScenarioTable getScenarioByPattern(String invokingString);

  Collection<ScenarioTable> getScenarios();

  void incrementPassedTestsCount();

  void incrementFailedTestsCount();

  void incrementErroredTestsCount();

  void incrementIgnoredTestsCount();

  void increment(ExecutionResult testSummary);

  void increment(TestSummary testSummary);

  TestPage getPageToTest();
}
