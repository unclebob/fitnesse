package fitnesse.plugins.slimcoverage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClient;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.testsystems.slim.tables.SyntaxError;

public class SlimCoverageTestSystem extends HtmlSlimTestSystem {
    private final ExecutionLogListener executionLogListener;
    private final SlimScenarioUsage usage;

    public SlimCoverageTestSystem(String testSystemName, ExecutionLogListener executionLogListener, SlimTableFactory slimTableFactory, CustomComparatorRegistry customComparatorRegistry) {
        super(testSystemName, dummySlimClient(), slimTableFactory, customComparatorRegistry);
        this.usage = new SlimScenarioUsage();
        this.executionLogListener = executionLogListener;
    }

    private static SlimClient dummySlimClient() {
        return new SlimClient() {
            @Override
            public void start() throws IOException {
            }

            @Override
            public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException {
                return null;
            }

            @Override
            public void connect() throws IOException {
            }

            @Override
            public void bye() throws IOException {
            }

            @Override
            public void kill() throws IOException {
            }
        };
    }

    public SlimScenarioUsage getUsage() {
        return usage;
    }

    @Override
    protected SlimTestContextImpl createTestContext(TestPage testPage) {
        String fullPath = testPage.getFullPath();
        SlimScenarioUsagePer usageByPage = usage.getUsageByPage(fullPath);
        return new SlimCoverageTestContextImpl(testPage, usageByPage);
    }

    @Override
    protected void processTable(SlimTable table) throws IOException, SyntaxError {
        table.getAssertions();
    }

    @Override
    protected void testStarted(TestPage testPage) throws IOException {
        super.testStarted(testPage);
        // ensure we have a single test passed, which is sometimes a requirement
        // (i.e. when run by FitNesseRunner)
        getTestContext().incrementPassedTestsCount();
    }

    @Override
    public void bye() throws IOException {
        try {
            super.bye();
        } finally {
            reportScenarioUsage();
        }
    }

    protected ExecutionLogListener getListener() {
        return executionLogListener;
    }

    protected void reportScenarioUsage() {
        ExecutionLogListener listener = getListener();
        listener.stdOut("Scenario Usage Report -------------------------------");
        listener.stdOut("");

        Map<String, Integer> totalUsage = usage.getScenarioUsage().getUsage();
        if (totalUsage.isEmpty()) {
            listener.stdOut("No scenarios in run");
        } else {
            Collection<String> unused = usage.getUnusedScenarios();
            if (!unused.isEmpty()) {
                listener.stdOut("Unused scenarios:");
                for (String scenarioName : unused) {
                    listener.stdOut(scenarioName);
                }
                listener.stdOut("\n");
            }

            listener.stdOut("Total usage count per scenario:");
            for (Map.Entry<String, Integer> totalUsageEntry : totalUsage.entrySet()) {
                listener.stdOut(totalUsageEntry.getKey()
                        + "\t"
                        + totalUsageEntry.getValue());
            }
            listener.stdOut("\n");

            listener.stdOut("Scenarios grouped by usage scope:");
            for (Map.Entry<String, Collection<String>> sByScopeEntry : usage.getScenariosBySmallestScope().entrySet()) {
                String scope = sByScopeEntry.getKey();
                listener.stdOut(scope);
                for (String scenario : sByScopeEntry.getValue()) {
                    listener.stdOut("\t"
                            + scenario);
                }
            }
            listener.stdOut("\n");

            listener.stdOut("Usage count per scenario per page:");
            for (SlimScenarioUsagePer usagePerPage : usage.getUsage()) {
                String pageName = usagePerPage.getGroupName();
                for (Map.Entry<String, Integer> usagePerScenario : usagePerPage.getUsage().entrySet()) {
                    listener.stdOut(pageName
                            + "\t"
                            + usagePerScenario.getKey()
                            + "\t"
                            + usagePerScenario.getValue());
                }
            }

            Map<String, Collection<String>> overriddenPerPage = usage.getOverriddenScenariosPerPage();
            if (!overriddenPerPage.isEmpty()) {
                listener.stdOut("\n");
                listener.stdOut("Overridden scenario(s) per page:");
                for (Map.Entry<String, Collection<String>> overriddenForPage : overriddenPerPage.entrySet()) {
                    String pageName = overriddenForPage.getKey();
                    for (String scenario : overriddenForPage.getValue()) {
                        listener.stdOut(pageName
                                + "\t"
                                + scenario);
                    }
                }
            }
        }
    }

}
