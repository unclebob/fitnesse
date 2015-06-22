package fitnesse.testsystems.slim;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.testsystems.slim.tables.SyntaxError;

public class CoverageSlimTestSystem extends HtmlSlimTestSystem {
    public CoverageSlimTestSystem(String testSystemName, ExecutionLogListener executionLogListener, SlimTableFactory slimTableFactory, CustomComparatorRegistry customComparatorRegistry) {
        super(testSystemName, dummySlimClient(executionLogListener), slimTableFactory, customComparatorRegistry, true);
    }

    private static SlimClient dummySlimClient(final ExecutionLogListener anExecutionLogListener) {
        return new SlimClient() {
            private ExecutionLogListener executionLogListener = anExecutionLogListener;
            @Override
            public void start() throws IOException {
                anExecutionLogListener.stdOut("Slim run checking usage of all defined scenarios");
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

            @Override
            public ExecutionLogListener getExecutionLogListener() {
                return executionLogListener;
            }
        };
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
}
