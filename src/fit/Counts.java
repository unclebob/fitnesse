// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public class Counts {
  public int right = 0;
  public int wrong = 0;
  public int ignores = 0;
  public int exceptions = 0;

  public Counts(int right, int wrong, int ignores, int exceptions) {
    this.right = right;
    this.wrong = wrong;
    this.ignores = ignores;
    this.exceptions = exceptions;
  }

  public Counts() {
  }

  public String toString() {
    return
      right + " right, " +
        wrong + " wrong, " +
        ignores + " ignored, " +
        exceptions + " exceptions";
  }

  public void tally(Counts source) {
    right += source.right;
    wrong += source.wrong;
    ignores += source.ignores;
    exceptions += source.exceptions;
  }

  public boolean equals(Object o) {
    if (o == null || !(o instanceof Counts))
      return false;
    Counts other = (Counts) o;
    return right == other.right &&
      wrong == other.wrong &&
      ignores == other.ignores &&
      exceptions == other.exceptions;
  }

  public void tallyPageCounts(Counts counts) {
    if (counts.wrong > 0)
      wrong += 1;
    else if (counts.exceptions > 0)
      exceptions += 1;
    else if (counts.ignores > 0 && counts.right == 0)
      ignores += 1;
    else
      right += 1;
  }
}
