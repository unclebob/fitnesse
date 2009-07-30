package fitnesse.responders.run;

import org.junit.Test;
import org.junit.Assert;

public class TestSummaryTest {
  @Test
  public void tallyPageCountsShouldCountPagesCorrectly() throws Exception {
    TestSummary pageCounts = new TestSummary(0, 0, 0, 0);
    pageCounts.tallyPageCounts(new TestSummary(32, 0, 0, 0)); // 1 right.
    pageCounts.tallyPageCounts(new TestSummary(0, 99, 0, 0)); // 1 wrong.
    pageCounts.tallyPageCounts(new TestSummary(0, 0, 0, 0)); // 1 ignore.
    pageCounts.tallyPageCounts(new TestSummary(0, 0, 0, 88)); // 1 exception.
    pageCounts.tallyPageCounts(new TestSummary(20, 1, 0, 0)); // 1 wrong;
    pageCounts.tallyPageCounts(new TestSummary(20, 20, 0, 20)); // 1 wrong;
    pageCounts.tallyPageCounts(new TestSummary(20, 0, 0, 20)); // 1 exception;
    Assert.assertEquals(new TestSummary(1, 3, 1, 2), pageCounts );
  }
}
