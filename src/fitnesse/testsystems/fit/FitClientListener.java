package fitnesse.testsystems.fit;

import fitnesse.testsystems.TestSummary;

import java.io.IOException;

public interface FitClientListener {
  void testOutputChunk(String readValue) throws IOException;

  void testComplete(TestSummary summary) throws IOException;

  void exceptionOccurred(Exception e);
}
