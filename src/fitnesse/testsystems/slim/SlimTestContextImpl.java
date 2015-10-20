// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.tables.ScenarioTable;

public class SlimTestContextImpl implements SlimTestContext {
  private final Map<String, String> symbols = new HashMap<String, String>();
  private final Map<String, ScenarioTable> scenarios = new HashMap<String, ScenarioTable>();
  private final TestSummary testSummary = new TestSummary();
  private final TestPage pageToTest;

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
    scenarios.put(scenarioName, scenarioTable);
  }

  @Override
  public ScenarioTable getScenario(String scenarioName) {
    return scenarios.get(scenarioName);
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
