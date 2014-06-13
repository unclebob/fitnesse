package fitnesse.testsystems.fit;

import java.io.IOException;

import fitnesse.testsystems.TestSummary;

public interface FitClientListener {
  void testOutputChunk(String readValue) throws IOException;

  void testComplete(TestSummary summary) throws IOException;

  void exceptionOccurred(Throwable e);
}
