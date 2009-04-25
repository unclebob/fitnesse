// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

public class TestSummary {
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

  public TestSummary(TestSummary testSummary) {
    this.right = testSummary.right;
    this.wrong = testSummary.wrong;
    this.ignores = testSummary.ignores;
    this.exceptions = testSummary.exceptions;
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

  public boolean equals(Object o) {
    if (o == null || !(o instanceof TestSummary))
      return false;
    TestSummary other = (TestSummary) o;
    return right == other.right &&
      wrong == other.wrong &&
      ignores == other.ignores &&
      exceptions == other.exceptions;
  }

  public void tallyPageCounts(TestSummary counts) {
    if (counts.wrong > 0)
      wrong += 1;
    else if (counts.exceptions > 0)
      exceptions += 1;
    else if (counts.right == 0)
      ignores += 1;
    else
      right += 1;
  }

  public void add(TestSummary testSummary) {
    right += testSummary.right;
    wrong += testSummary.wrong;
    ignores += testSummary.ignores;
    exceptions += testSummary.exceptions;
  }

  public void clear() {
    right = 0;
    wrong = 0;
    ignores = 0;
    exceptions = 0;
  }
}
