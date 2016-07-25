package fitnesse.plugins.slimcoverage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.UnableToStopException;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClient;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageDummy;

public class SlimCoverageTestSystem extends HtmlSlimTestSystem {
    private final SlimScenarioUsage usage;

    public SlimCoverageTestSystem(String testSystemName, SlimTableFactory slimTableFactory, CustomComparatorRegistry customComparatorRegistry) {
        super(testSystemName, dummySlimClient(), slimTableFactory, customComparatorRegistry);
        this.usage = new SlimScenarioUsage();
    }

    private static SlimClient dummySlimClient() {
        return new SlimClient() {
            @Override
            public void start() {
            }

            @Override
            public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) {
                return null;
            }

            @Override
            public void connect() {
            }

            @Override
            public void bye() {
            }

            @Override
            public void kill() {
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
    protected void processTable(SlimTable table, boolean isSuiteTearDownPage) throws TestExecutionException {
        table.getAssertions();
    }

    @Override
    protected void testStarted(TestPage testPage) {
        super.testStarted(testPage);
        // ensure we have a single test passed, which is sometimes a requirement
        // (i.e. when run by FitNesseRunner)
        getTestContext().incrementPassedTestsCount();
    }

    @Override
    public void bye() throws UnableToStopException {
        try {
            reportScenarioUsage();
        } finally {
            super.bye();
        }
    }

    protected void reportScenarioUsageHeader(String header) {
        testOutputChunk("<h4>" + header + "</h4>");
    }

    protected void reportScenarioUsageNewline() {
        testOutputChunk("<br/>");
    }

    protected void reportScenarioUsage() {
        WikiPageDummy pageDummy = new WikiPageDummy("Scenario Usage Report", "Scenario Usage Report Content", null);
        WikiTestPage testPage = new WikiTestPage(pageDummy);
        testStarted(testPage);

        Map<String, Integer> totalUsage = usage.getScenarioUsage().getUsage();
        if (totalUsage.isEmpty()) {
            testOutputChunk("No scenarios in run");
        } else {
            Collection<String> unused = usage.getUnusedScenarios();
            if (!unused.isEmpty()) {
                reportScenarioUsageHeader("Unused scenarios:");
                testOutputChunk("<ul>");
                for (String scenarioName : unused) {
                    testOutputChunk("<li>" + scenarioName + "</li>");
                }
                testOutputChunk("</ul>");
                reportScenarioUsageNewline();
            }

            reportScenarioUsageHeader("Total usage count per scenario:");
            testOutputChunk("<table>");
            testOutputChunk("<tr><th>Scenario</th><th>Count</th></tr>");
            for (Map.Entry<String, Integer> totalUsageEntry : totalUsage.entrySet()) {
                testOutputChunk("<tr>");
                testOutputChunk("<td>");
                testOutputChunk(totalUsageEntry.getKey()
                        + "</td><td>"
                        + totalUsageEntry.getValue());
                testOutputChunk("</td>");
                testOutputChunk("</tr>");
            }
            testOutputChunk("</table>");
            reportScenarioUsageNewline();

            reportScenarioUsageHeader("Scenarios grouped by usage scope:");
            testOutputChunk("<ul>");
            for (Map.Entry<String, Collection<String>> sByScopeEntry : usage.getScenariosBySmallestScope().entrySet()) {
                String scope = sByScopeEntry.getKey();
                testOutputChunk("<li>");
                testOutputChunk(scope);
                testOutputChunk("<ul>");
                for (String scenario : sByScopeEntry.getValue()) {
                    testOutputChunk("<li>" + scenario + "</li>");
                }
                testOutputChunk("</ul>");
                testOutputChunk("</li>");
            }
            testOutputChunk("</ul>");
            reportScenarioUsageNewline();

            reportScenarioUsageHeader("Usage count per scenario per page:");
            testOutputChunk("<table>");
            testOutputChunk("<tr><th>Page</th><th>Scenario</th><th>Count</th></tr>");
            for (SlimScenarioUsagePer usagePerPage : usage.getUsage()) {
                String pageName = usagePerPage.getGroupName();
                for (Map.Entry<String, Integer> usagePerScenario : usagePerPage.getUsage().entrySet()) {
                    testOutputChunk("<tr>");
                    testOutputChunk("<td>");
                    testOutputChunk(pageName
                            + "</td><td>"
                            + usagePerScenario.getKey()
                            + "</td><td>"
                            + usagePerScenario.getValue());
                    testOutputChunk("</td>");
                    testOutputChunk("</tr>");
                }
            }
            testOutputChunk("</table>");

            Map<String, Collection<String>> overriddenPerPage = usage.getOverriddenScenariosPerPage();
            if (!overriddenPerPage.isEmpty()) {
                reportScenarioUsageNewline();
                reportScenarioUsageHeader("Overridden scenario(s) per page:");
                testOutputChunk("<ul>");
                for (Map.Entry<String, Collection<String>> overriddenForPage : overriddenPerPage.entrySet()) {
                    String pageName = overriddenForPage.getKey();
                    testOutputChunk("<li>");
                    testOutputChunk(pageName);
                    testOutputChunk("<ul>");
                    for (String scenario : overriddenForPage.getValue()) {
                        testOutputChunk("<li>" + scenario + "</li>");
                    }
                    testOutputChunk("</ul>");
                    testOutputChunk("</li>");
                }
                testOutputChunk("</ul>");
            }
        }
        testComplete(testPage, new TestSummary(0, 0, 1, 0));
    }

}
