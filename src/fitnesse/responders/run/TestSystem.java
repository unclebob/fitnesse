package fitnesse.responders.run;

import fitnesse.wiki.PageData;

public interface TestSystem {
  void sendPageData(PageData pageData) throws Exception;

  void bye() throws Exception;

  void kill() throws Exception;

  void setFastTest(boolean fastTest);

  boolean isSuccessfullyStarted();

  public static class TestSummary {
    public int right = 0;
    public int wrong = 0;
    public int ignores = 0;
    public int exceptions = 0;

    public TestSummary(int right, int wrong, int ignores, int exceptions) {
      this.right = right;
      this.wrong = wrong;
      this.ignores = ignores;
      this.exceptions = exceptions;
    }

    public TestSummary() {
    }

    public String toString() {
      return
        right + " right, " +
          wrong + " wrong, " +
          ignores + " ignored, " +
          exceptions + " exceptions";
    }

    public void tally(TestSystemBase.TestSummary source) {
      right += source.right;
      wrong += source.wrong;
      ignores += source.ignores;
      exceptions += source.exceptions;
    }

    public boolean equals(Object o) {
      if (o == null || !(o instanceof TestSystemBase.TestSummary))
        return false;
      TestSystemBase.TestSummary other = (TestSystemBase.TestSummary) o;
      return right == other.right &&
        wrong == other.wrong &&
        ignores == other.ignores &&
        exceptions == other.exceptions;
    }

    public void tallyPageCounts(TestSystemBase.TestSummary counts) {
      if (counts.wrong > 0)
        wrong += 1;
      else if (counts.exceptions > 0)
        exceptions += 1;
      else if (counts.ignores > 0 && counts.right == 0)
        ignores += 1;
      else
        right += 1;
    }

    public void add(TestSystemBase.TestSummary testSummary) {
      right += testSummary.right;
      wrong += testSummary.wrong;
      ignores += testSummary.ignores;
      exceptions += testSummary.exceptions;
    }
  }
}
