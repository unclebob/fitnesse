/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.IOException;

import fit.Counts;

public class TestRunner {
  private TestRepository repository;
  private TestEngine testRunner;
  private TestResultRepository resultRepository;

  public TestRunner(TestRepository repository, TestEngine testRunner,
      String outputPath) throws IOException {
    this(repository, testRunner, new FolderTestResultRepository(outputPath));
  }

  public TestRunner(TestRepository repository, TestEngine testRunner,
      TestResultRepository resultRepository) throws IOException {
    this.repository = repository;
    this.testRunner = testRunner;
    this.resultRepository = resultRepository;
    repository.prepareResultRepository(resultRepository);
  }

  public Counts runTest(String testUrl) throws IOException {
    TestResult tr = testRunner.runTest(repository.getTest(testUrl));
    resultRepository.recordTestResult(tr);
    sleep();
    return tr.getCounts();
  }

  public Counts runSuite(String suite) throws IOException {
    SuiteResult suiteResult = new SuiteResult(suite);
    for (TestDescriptor t : repository.getSuite(suite)) {
      TestResult tr = testRunner.runTest(t);
      suiteResult.append(tr);
      resultRepository.recordTestResult(tr);
      sleep();
    }
    resultRepository.recordTestResult(suiteResult);
    return suiteResult.getCounts();
  }

  private void sleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }
}
