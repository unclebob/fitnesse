package fitnesse.responders.run;

import static fitnesse.responders.run.SuiteExecutionReport.PageHistoryReference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class SuiteExecutionReportTest {
  private SuiteExecutionReport report1;
  private SuiteExecutionReport report2;

   @Before
  public void setUp() throws Exception {
    report1 = new SuiteExecutionReport();
    report2 = new SuiteExecutionReport();
  }

  @Test
  public void degeneratesShouldBeEqual() throws Exception {
    assertEquals(new SuiteExecutionReport(), new SuiteExecutionReport());
  }
  @Test
  public void shouldNotBeEqualIfDifferentTypes() throws Exception {
    assertFalse(new SuiteExecutionReport().equals(new Integer(0)));
  }

  @Test
  public void shouldNotBeEqualWithDifferentRootPaths()throws Exception
  {
    report1.rootPath = "here";
    report2.rootPath = "there";
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shouldNotBeEqualIfHaveDifferentReferences() throws Exception {
    report1.addPageHistoryReference(new PageHistoryReference("pageOne",1234));
    report2.addPageHistoryReference(new PageHistoryReference("pageTwo",1234));
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shouldBeEqualIfReferencesAreTheSame() throws Exception {
    PageHistoryReference r1 = new PageHistoryReference("TestPage", 1111);
    PageHistoryReference r2 = new PageHistoryReference("TestPage", 1111);
    r1.getTestSummary().right = 3;
    r2.getTestSummary().right = 3;
    report1.addPageHistoryReference(r1);
    report2.addPageHistoryReference(r2);
    assertEquals(report1, report2);
  }

  @Test
  public void shouldNotBeEqualIfVersionIsDifferent() throws Exception {
    report1.version = "x";
    report2.version = "y";
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shoudlNotBeEqualIfDateIsDifferent() throws Exception {
    report1.date = new Date(1);
    report2.date = new Date(2);
    assertFalse(report1.equals(report2));
  }

  @Test
  public void shouldNotBeEqualIfFinalCountsAreDifferent() throws Exception {
    report1.finalCounts = new TestSummary(1,2,3,4);
    report2.finalCounts = new TestSummary(4,3,2,1);
    assertFalse(report1.equals(report2));    
  }

  @Test
  public void shoudlBeEqualWithAllFieldsEqual() throws Exception {
    report1.version = report2.version = "version";
    report1.date = report2.date = new Date(1);
    report1.finalCounts = report2.finalCounts = new TestSummary(4,5,6,7);
    report1.rootPath = report2.rootPath = "rootPath";
    PageHistoryReference r1a = new PageHistoryReference("testPage", 1234);
    PageHistoryReference r2a = new PageHistoryReference("testPage", 1234);
    PageHistoryReference r1b = new PageHistoryReference("myPage", 7734);
    PageHistoryReference r2b = new PageHistoryReference("myPage", 7734);
    r1a.getTestSummary().right=4;
    r2a.getTestSummary().right=4;
    report1.addPageHistoryReference(r1a);
    report1.addPageHistoryReference(r1b);
    report2.addPageHistoryReference(r2a);
    report2.addPageHistoryReference(r2b);
    assertEquals(report1, report2);
  }
}
