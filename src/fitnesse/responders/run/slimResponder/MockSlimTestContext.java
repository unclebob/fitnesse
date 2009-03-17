// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.slimTables.ScenarioTable;
import fitnesse.slimTables.SlimTable;

public class MockSlimTestContext implements SlimTestContext {
  private Map<String, String> symbols = new HashMap<String, String>();
  private Map<String, ScenarioTable> scenarios = new HashMap<String, ScenarioTable>();
  private List<SlimTable.Expectation> expectations = new ArrayList<SlimTable.Expectation>();

  public String getSymbol(String symbolName) {
    return symbols.get(symbolName);
  }

  public void setSymbol(String symbolName, String value) {
    symbols.put(symbolName, value);
  }

  public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
    scenarios.put(scenarioName, scenarioTable);
  }

  public ScenarioTable getScenario(String scenarioName) {
    return scenarios.get(scenarioName);
  }

  public void addExpectation(SlimTable.Expectation e) {
    expectations.add(e);
  }

  public void evaluateExpectations(Map<String, Object> results) {
    for (SlimTable.Expectation e : expectations)
      e.evaluateExpectation(results);
  }
}
