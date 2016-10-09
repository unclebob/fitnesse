// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.tables.ScenarioTable;

public class SlimTestContextImpl implements SlimTestContext {
  private final Map<String, String> symbols = new HashMap<>();
  private final Map<String, ScenarioTable> scenarios = new HashMap<>(512);
  private final TestSummary testSummary = new TestSummary();
  private final TestPage pageToTest;
  private List<ScenarioTable> scenariosWithInputs = null;
  private boolean isSorted = true;

  public SlimTestContextImpl(TestPage pageToTest) {
    this.pageToTest = pageToTest;
  }

  @Override
  public String getSymbol(String symbolName) {
    return symbols.get(symbolName);
  }

  @Override
  public void setSymbol(String symbolName, String value) {
    symbols.put(symbolName, value);
  }

  @Override
  public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
    ScenarioTable oldValue = scenarios.put(scenarioName, scenarioTable);
    if (scenariosWithInputs != null) {
      maintainScenariosWithInputs(oldValue, scenarioTable);
    }
  }

  @Override
  public ScenarioTable getScenario(String scenarioName) {
    return scenarios.get(scenarioName);
  }

  @Override
  public ScenarioTable getScenarioByPattern(String invokingString) {
    ScenarioTable result = null;
    for (ScenarioTable s : getScenariosWithMostArgumentsFirst()) {
      if (s.canMatchParameters(invokingString)) {
        result = s;
        break;
      }
    }
    return result;
  }

  private List<ScenarioTable> getScenariosWithMostArgumentsFirst() {
    if (scenariosWithInputs == null) {
      initializeScenariosWithInputs();
    }
    if (!isSorted) {
      Collections.sort(scenariosWithInputs, new ScenarioTableLengthComparator());
      isSorted = true;
    }
    return scenariosWithInputs;
  }

  private static class ScenarioTableLengthComparator implements Comparator<ScenarioTable> {
    @Override
    public int compare(ScenarioTable st1, ScenarioTable st2) {
      int size1 = st1.getInputs().size();
      int size2 = st2.getInputs().size();
      return size2 - size1;
    }
  }

  private void initializeScenariosWithInputs() {
    int initialCapacity = scenarios.size();
    scenariosWithInputs = new ArrayList<>(initialCapacity);
    isSorted = true;
    for (ScenarioTable table : scenarios.values()) {
      addToScenariosWithInputsIfNeeded(table);
    }
  }

  private void maintainScenariosWithInputs(ScenarioTable oldTable, ScenarioTable newTable) {
    if (oldTable != null && !oldTable.getInputs().isEmpty()) {
      scenariosWithInputs.remove(oldTable);
    }
    addToScenariosWithInputsIfNeeded(newTable);
  }

  private void addToScenariosWithInputsIfNeeded(ScenarioTable newTable) {
    if (!newTable.getInputs().isEmpty()) {
      scenariosWithInputs.add(newTable);
      isSorted = false;
    }
  }

  @Override
  public Collection<ScenarioTable> getScenarios() {
    return scenarios.values();
  }

  @Override
  public void incrementPassedTestsCount() {
    increment(ExecutionResult.PASS);
  }

  @Override
  public void incrementFailedTestsCount() {
    increment(ExecutionResult.FAIL);
  }

  @Override
  public void incrementErroredTestsCount() {
    increment(ExecutionResult.ERROR);
  }

  @Override
  public void incrementIgnoredTestsCount() {
    increment(ExecutionResult.IGNORE);
  }

  @Override
  public void increment(ExecutionResult result) {
    this.testSummary.add(result);
  }

  @Override
  public void increment(TestSummary testSummary) {
    this.testSummary.add(testSummary);
  }

  public TestSummary getTestSummary() {
    return testSummary;
  }

  @Override
  public TestPage getPageToTest() {
    return pageToTest;
  }
}
