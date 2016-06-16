package fitnesse.testsystems.slim;

import java.util.Collection;
import java.util.Map;

import fitnesse.plugins.slimcoverage.SlimScenarioUsage;
import fitnesse.plugins.slimcoverage.SlimScenarioUsagePer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SlimScenarioUsageTest {
  private SlimScenarioUsage usage = new SlimScenarioUsage();

  @Test
  public void testScenariosWithSmallestScope() {
    String page1 = "Suite1.Page1";
    String page2 = "Suite1.SuiteA.Page1";
    String page3 = "Suite1.SuiteA.Page2";
    String page4 = "Suite1.SuiteAPage1.Page1";
    String page5 = "Suite1.SuiteAPage1.Page2";

    String globalScenario = "global";
    String localScenario = "localPage1";
    String localScenario2 = "Suite1.SuiteA.Page2";
    String suiteAScenario1 = "suiteAScen1";
    String suiteAScenario2 = "suiteAScen2";
    String suiteAPage1Scenario = "suiteA1";
    String suiteAPageUnusedScenario = "suiteA2";

    SlimScenarioUsagePer page1U = usage.getUsageByPage(page1);
    page1U.addUsage(globalScenario, 1);
    page1U.addUsage(localScenario, 1);

    SlimScenarioUsagePer page2U = usage.getUsageByPage(page2);
    page2U.addUsage(suiteAScenario1, 1);
    page2U.addUsage(suiteAScenario2, 2);

    SlimScenarioUsagePer page3U = usage.getUsageByPage(page3);
    page3U.addUsage(globalScenario, 1);
    page3U.addUsage(suiteAScenario1, 2);
    page3U.addUsage(suiteAScenario2, 1);

    SlimScenarioUsagePer page4U = usage.getUsageByPage(page4);
    page4U.addUsage(globalScenario, 2);
    page4U.addDefinition(suiteAPageUnusedScenario);
    page4U.addUsage(suiteAPage1Scenario, 1);
    page4U.addUsage(localScenario2, 1);

    SlimScenarioUsagePer page5U = usage.getUsageByPage(page5);
    page5U.addDefinition(suiteAPageUnusedScenario);
    page5U.addUsage(suiteAPage1Scenario, 1);

    Map<String, Collection<String>> scenariosPerScope = usage.getScenariosBySmallestScope();

    assertScopePresent(scenariosPerScope, "Suite1");
    assertScopePresent(scenariosPerScope, page1);
    assertScopePresent(scenariosPerScope, "Suite1.SuiteA");
    assertScopePresent(scenariosPerScope, "Suite1.SuiteAPage1");
    assertScopePresent(scenariosPerScope, page4);
    assertEquals("Scopes: " + scenariosPerScope.toString(), 5, scenariosPerScope.size());

    assertScopeContains(scenariosPerScope, "Suite1", globalScenario);
    assertScopeContains(scenariosPerScope, page1, localScenario);
    assertScopeContains(scenariosPerScope, "Suite1.SuiteA", suiteAScenario1, suiteAScenario2);
    assertScopeContains(scenariosPerScope, "Suite1.SuiteAPage1", suiteAPage1Scenario);
    assertScopeContains(scenariosPerScope, page4, localScenario2);
  }

  private void assertScopeContains(Map<String, Collection<String>> scenariosPerScope, String scopeName, String... scenarios) {
    Collection<String> scope = scenariosPerScope.get(scopeName);
    for (String scenario : scenarios) {
      assertTrue(scope + " did not contain " + scenario, scope.contains(scenario));
    }
    assertEquals("Wrong number of scenarios in " + scopeName + ": " + scope, scenarios.length, scope.size());
  }

  private void assertScopePresent(Map<String, Collection<String>> scenariosPerScope, String scopeName) {
    assertTrue(scenariosPerScope + " did not contain " + scopeName, scenariosPerScope.containsKey(scopeName));
  }
}
