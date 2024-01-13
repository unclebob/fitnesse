package fitnesse.junit;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestFailure;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class JUnitXMLPerPageTestListenerTest {

  private JUnitXMLTestResultRecorder testResultRecorderMock = mock(JUnitXMLTestResultRecorder.class);

  // SUT
  private JUnitXMLPerPageTestListener jUnitXMLPerPageTestListener = new JUnitXMLPerPageTestListener(testResultRecorderMock);

  @Test
  public void beforeSuite() {
    // given a before suite event is triggered with a test descriptor
    TestDescriptor testDescriptor = getTestDescriptor();

    // when beforeSuite is called
    jUnitXMLPerPageTestListener.beforeSuite(testDescriptor);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void afterSuiteOnSuccess() {
    // given a after test event is triggered with a test descriptor and a test success
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.SUCCESS, null);

    // when afterSuite is called
    jUnitXMLPerPageTestListener.afterSuite(testDescriptor, testResult);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void afterSuiteOnSkipped() {
    // given a after test event is triggered with a test descriptor and a skipped test
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.SKIPPED, new AssertionError());

    // when afterSuite is called
    jUnitXMLPerPageTestListener.afterSuite(testDescriptor, testResult);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void afterSuiteOnSkippedWithError() {
    // given a after test event is triggered with a test descriptor and a skipped test
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.SKIPPED, new RuntimeException());

    // when afterSuite is called
    jUnitXMLPerPageTestListener.afterSuite(testDescriptor, testResult);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void afterSuiteOnFailure() {
    // given a after test event is triggered with a test descriptor and a test failure
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.FAILURE, new AssertionError());

    // when afterSuite is called
    jUnitXMLPerPageTestListener.afterSuite(testDescriptor, testResult);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void afterSuiteOnFailureWithError() {
    // given a after test event is triggered with a test descriptor and a test failure
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.FAILURE, new RuntimeException());

    // when afterSuite is called
    jUnitXMLPerPageTestListener.afterSuite(testDescriptor, testResult);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void beforeTest() {
    // given a before test event is triggered with a test descriptor
    TestDescriptor testDescriptor = getTestDescriptor();

    // when beforeTest is called
    jUnitXMLPerPageTestListener.beforeTest(testDescriptor);

    // then does nothing
    verifyNoInteractions(testResultRecorderMock);
  }

  @Test
  public void afterTestOnSuccess() {
    // given a after test event is triggered with a test descriptor and a test success
    TestDescriptor testDescriptor = getTestDescriptor();
    TestResult testResult = getTestResult(TestResult.ResultType.SUCCESS, null);

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(testDescriptor.getName(), 0, 0, 0, null, testResult.getEndTime() - testResult.getStartTime());
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

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(testDescriptor.getName(), 1, 0, 0, throwable, testResult.getEndTime() - testResult.getStartTime());
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

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(testDescriptor.getName(), 0, 0, 1, throwable, testResult.getEndTime() - testResult.getStartTime());
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

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(testDescriptor.getName(), 0, 1, 0, throwable, testResult.getEndTime() - testResult.getStartTime());
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

    // when afterTest is called
    jUnitXMLPerPageTestListener.afterTest(testDescriptor, testResult);

    // then the test result recorder is called with the correct parameters and no exception IOException is thrown
    try {
      verify(testResultRecorderMock)
        .recordTestResult(testDescriptor.getName(), 0, 0, 1, throwable, testResult.getEndTime() - testResult.getStartTime());
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
      public List<TestFailure> getFailures() {
        return null;
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
