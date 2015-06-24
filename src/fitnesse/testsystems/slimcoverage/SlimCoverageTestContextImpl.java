// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slimcoverage;

import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.tables.ScenarioTable;

public class SlimCoverageTestContextImpl extends SlimTestContextImpl {
  private final SlimScenarioUsagePer usage;

  public SlimCoverageTestContextImpl(SlimScenarioUsagePer usageByPage) {
    usage = usageByPage;
  }

  @Override
  public void addScenario(String scenarioName, ScenarioTable scenarioTable) {
    if (usage != null) {
      usage.addDefinition(scenarioName);
    }
    super.addScenario(scenarioName, scenarioTable);
  }

  public ScenarioTable getScenario(String scenarioName) {
    ScenarioTable scenarioTable = super.getScenario(scenarioName);
    if (usage != null && scenarioTable != null) {
      usage.addUsage(scenarioName);
    }
    return scenarioTable;
  }
}
