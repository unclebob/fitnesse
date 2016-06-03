package fitnesse.testsystems.fit;

import fitnesse.testsystems.TestSummary;

public interface FitClientListener {
  void testOutputChunk(String readValue);

  void testComplete(TestSummary summary);

  void exceptionOccurred(Throwable e);
}
