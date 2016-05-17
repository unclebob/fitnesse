package fitnesse.testsystems;

import org.junit.Assert;
import org.junit.Test;

import static fitnesse.testsystems.ExecutionResult.getExecutionResult;

public class TestSummaryTest {
  @Test
  public void tallyPageCountsShouldCountPagesCorrectly() throws Exception {
    TestSummary pageCounts = new TestSummary(0, 0, 0, 0);
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(32, 0, 0, 0))); // 1 right.
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(0, 99, 0, 0))); // 1 wrong.
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(0, 0, 0, 0))); // 1 ignore.
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(0, 0, 0, 88))); // 1 exception.
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(20, 1, 0, 0))); // 1 wrong;
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(20, 20, 0, 20))); // 1 wrong;
    pageCounts.add(getExecutionResult("TestPage", new TestSummary(20, 0, 0, 20))); // 1 exception;
    Assert.assertEquals(new TestSummary(1, 3, 1, 2), pageCounts );
  }
}
