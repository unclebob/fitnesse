// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.slim.tables.Expectation;
import fitnesse.testsystems.slim.tables.ScenarioTable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SlimTestContext {
  String getSymbol(String symbolName);

  void setSymbol(String symbolName, String value);

  void addScenario(String scenarioName, ScenarioTable scenarioTable);

  ScenarioTable getScenario(String scenarioName);

  @Deprecated
  void addExpectation(Expectation e);

  Collection<ScenarioTable> getScenarios();

  void incrementPassedTestsCount();

  void incrementFailedTestsCount();

  void incrementErroredTestsCount();

  void incrementIgnoredTestsCount();
}
