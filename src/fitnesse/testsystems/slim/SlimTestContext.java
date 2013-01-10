// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.testsystems.slim.tables.ScenarioTable;
import fitnesse.testsystems.slim.tables.SlimTable;

import java.util.Map;

public interface SlimTestContext {
  String getSymbol(String symbolName);

  void setSymbol(String symbolName, String value);

  void addScenario(String scenarioName, ScenarioTable scenarioTable);

  ScenarioTable getScenario(String scenarioName);

  void addExpectation(SlimTable.Expectation e);

  Map<String, ScenarioTable> getScenarios();
}
