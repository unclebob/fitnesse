package fitnesse.junit;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JUnitXMLPerPageTestListenerIntegrationTest {

  @Rule
  public TemporaryFolder reportDir = new TemporaryFolder();

  private JUnitXMLReportHelper jUnitXMLReportHelper;

  // SUT
  private JUnitXMLPerPageTestListener jUnitXMLPerPageTestListener;

  @Before
  public void setUp() {
    jUnitXMLPerPageTestListener = new JUnitXMLPerPageTestListener(new JUnitXMLTestResultRecorder(reportDir.getRoot()));
    jUnitXMLReportHelper = new JUnitXMLReportHelper(reportDir.getRoot());
  }

  @Test
  public void afterTestOnSuccess() {
    // given a after test event is triggered with a test descriptor and a test success
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.SUCCESS, null);
    String xmlResultOnSuccess = jUnitXMLReportHelper
      .getXmlResultOnSuccess(testDescriptor.getName(), testResult.getEndTime() - testResult.getStartTime());

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the correct report is written to disk
    try {
      assertEquals(xmlResultOnSuccess, jUnitXMLReportHelper.readReportFile("TEST-" + testDescriptor.getName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void afterTestOnSkipped() {
    // given a after test event is triggered with a test descriptor and a skipped test
    TestDescriptor testDescriptor = getTestDescriptor();
    Throwable throwable = new AssertionError("Gallier approved detail messsage");
    TestResult testResult = getTestResult(TestResult.ResultType.SKIPPED, throwable);
    String xmlResultOnSkipped = jUnitXMLReportHelper
      .getXmlResultOnSkipped(testDescriptor.getName(), testResult.getEndTime() - testResult.getStartTime(), throwable);

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the correct report is written to disk
    try {
      assertEquals(xmlResultOnSkipped, jUnitXMLReportHelper.readReportFile("TEST-" + testDescriptor.getName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void afterTestOnSkippedWithError() {
    // given a after test event is triggered with a test descriptor and a skipped test
    TestDescriptor testDescriptor = getTestDescriptor();
    Throwable throwable = new RuntimeException("Gallier approved detail messsage");
    TestResult testResult = getTestResult(TestResult.ResultType.SKIPPED, throwable);
    String xmlResultOnError = jUnitXMLReportHelper
      .getXmlResultOnError(testDescriptor.getName(), testResult.getEndTime() - testResult.getStartTime(), throwable);

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the correct report is written to disk
    try {
      assertEquals(xmlResultOnError, jUnitXMLReportHelper.readReportFile("TEST-" + testDescriptor.getName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void afterTestOnFailure() {
    // given a after test event is triggered with a test descriptor and a failed test
    TestDescriptor testDescriptor = getTestDescriptor();
    Throwable throwable = new AssertionError("Gallier approved detail messsage");
    TestResult testResult = getTestResult(TestResult.ResultType.FAILURE, throwable);
    String xmlResultOnFailure = jUnitXMLReportHelper
      .getXmlResultOnFailure(testDescriptor.getName(), testResult.getEndTime() - testResult.getStartTime(), throwable);

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the correct report is written to disk
    try {
      assertEquals(xmlResultOnFailure, jUnitXMLReportHelper.readReportFile("TEST-" + testDescriptor.getName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  @Test
  public void afterTestOnFailureWithError() {
    // given a after test event is triggered with a test descriptor and a failed test
    TestDescriptor testDescriptor = getTestDescriptor();
    Throwable throwable = new RuntimeException("Gallier approved detail messsage");
    TestResult testResult = getTestResult(TestResult.ResultType.FAILURE, throwable);
    String xmlResultOnError = jUnitXMLReportHelper
      .getXmlResultOnError(testDescriptor.getName(), testResult.getEndTime() - testResult.getStartTime(), throwable);

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the correct report is written to disk
    try {
      assertEquals(xmlResultOnError, jUnitXMLReportHelper.readReportFile("TEST-" + testDescriptor.getName() + ".xml"));
    } catch (IOException e) {
      fail("IOException was caught but should have never been thrown.");
    }
  }

  private TestDescriptor getTestDescriptor() {
    return new TestDescriptor() {
      @Override
      public String getName() {
        return "myTestName";
      }

      @Override
      public String getDisplayName() {
        return "myTestDisplayName";
      }

      @Nullable
      @Override
      public String getClassName() {
        return TestDescriptor.class.getName();
      }

      @Override
      public boolean isComposite() {
        return false;
      }

      @Nullable
      @Override
      public TestDescriptor getParent() {
        return null;
      }
    };
  }

  private TestResult getTestResult(TestResult.ResultType resultType, Throwable throwable) {
    return new TestResult() {
      @Override
      public ResultType getResultType() {
        return resultType;
      }

      @Nullable
      @Override
      public Throwable getException() {
        return throwable;
      }

      @Override
      public List<Throwable> getExceptions() {
        return Collections.singletonList(throwable);
      }

      @Override
      public long getStartTime() {
        return 0;
      }

      @Override
      public long getEndTime() {
        return 1275;
      }

      @Override
      public long getTestCount() {
        return 0; // does not matter
      }

      @Override
      public long getSuccessfulTestCount() {
        return 0; // does not matter
      }

      @Override
      public long getFailedTestCount() {
        return 0; // does not matter
      }

      @Override
      public long getSkippedTestCount() {
        return 0; // does not matter
      }
    };
  }
}
