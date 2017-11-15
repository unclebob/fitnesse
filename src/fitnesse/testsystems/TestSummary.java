// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

public class TestSummary {
  private int right = 0;
  private int wrong = 0;
  private int ignores = 0;
  private int exceptions = 0;
  private long runTimeInMillis = 0;

  public TestSummary(int right, int wrong, int ignores, int exceptions) {
    this.right = right;
    this.wrong = wrong;
    this.ignores = ignores;
    this.exceptions = exceptions;
  }

  public TestSummary(TestSummary testSummary) {
    right = testSummary.getRight();
    wrong = testSummary.getWrong();
    ignores = testSummary.getIgnores();
    exceptions = testSummary.getExceptions();
    runTimeInMillis = testSummary.getRunTimeInMillis();
  }

  public TestSummary() {
  }

  @Override
  public String toString() {
    return getRight() + " right, " + getWrong() + " wrong, " + getIgnores()
    + " ignored, " + getExceptions() + " exceptions";
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof TestSummary))
      return false;
    TestSummary other = (TestSummary) o;
    return getRight() == other.getRight() && getWrong() == other.getWrong()
    && getIgnores() == other.getIgnores()
    && getExceptions() == other.getExceptions() && getRunTimeInMillis() == other.getRunTimeInMillis();
  }

  @Override
  public int hashCode() {
    assert false : "hashCode not designed";
    return 42;
  }

  public void add(TestSummary testSummary) {
    right = getRight() + testSummary.getRight();
    wrong = getWrong() + testSummary.getWrong();
    ignores = getIgnores() + testSummary.getIgnores();
    exceptions = getExceptions() + testSummary.getExceptions();
    runTimeInMillis = getRunTimeInMillis() + testSummary.getRunTimeInMillis();
  }

  public void clear() {
    right = 0;
    wrong = 0;
    ignores = 0;
    exceptions = 0;
    runTimeInMillis = 0;
  }

  public int getRight() {
    return right;
  }

  public int getWrong() {
    return wrong;
  }

  public int getIgnores() {
    return ignores;
  }

  public int getExceptions() {
    return exceptions;
  }

  public long getRunTimeInMillis() { return runTimeInMillis; }

  public void add(ExecutionResult executionResult) {
    if (executionResult != null) {
     switch (executionResult) {
       case PASS:
         right++;
         break;
       case FAIL:
         wrong++;
         break;
       case IGNORE:
         ignores++;
         break;
       case ERROR:
         exceptions++;
         break;
     }
    }
  }

  public void setRunTimeInMillis(long runTimeInMillis) {
    this.runTimeInMillis = runTimeInMillis;
  }
}
