// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling;

public class BowlingScorer {
  private int[] rolls = new int[21];
  protected int rollNumber = 0;

  public void roll(int pins) {
    rolls[rollNumber++] = pins;
  }

  public int score(int frame) {
    int score = 0;
    int roll = 0;
    for (int f = 0; f < frame; f++) {
      if (strike(roll)) {
        score += 10 + nextTwoBallsForStrike(roll);
        roll++;
      } else if (spare(roll)) {
        score += 10 + nextBallForSpare(roll);
        roll += 2;
      } else {
        score += ballsInFrame(roll);
        roll += 2;
      }
    }
    return score;
  }

  private int ballsInFrame(int roll) {
    return rolls[roll] + rolls[roll + 1];
  }

  private int nextBallForSpare(int roll) {
    return rolls[roll + 2];
  }

  private int nextTwoBallsForStrike(int roll) {
    return (rolls[roll + 1] + rolls[roll + 2]);
  }

  private boolean spare(int roll) {
    return rolls[roll] + rolls[roll + 1] == 10;
  }

  private boolean strike(int roll) {
    return rolls[roll] == 10;
  }

  protected boolean lastRollWasStrike() {
    return rolls[rollNumber - 1] == 10;
  }

  protected boolean lastRollWasSpare() {
    return rolls[rollNumber - 2] + rolls[rollNumber - 1] == 10;
  }
}
