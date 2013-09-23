// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import static util.ListUtility.list;

import java.util.List;

public class Bowling {
  private Game g;

  @SuppressWarnings("unchecked")
  public List<?> doTable(List<Object> table) {
    g = new Game();
    List<?> rollResults = list("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    List<String> scoreResults = list("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    rollBalls(table);
    evaluateScores(g, (List<String>)table.get(1), scoreResults);
    return list(rollResults, scoreResults);
  }

  private void evaluateScores(Game g, List<String> object, List<String> scoreResults) {
    for (int frame = 0; frame < 10; frame++) {
      int actualScore = g.score(frame + 1);
      int expectedScore = Integer.parseInt((String)object.get(frameCoordinate(frame)));
      if (expectedScore == actualScore)
        scoreResults.set(frameCoordinate(frame), "pass");
      else
        scoreResults.set(frameCoordinate(frame), String.format("Was:%d, expected:%s.", actualScore, expectedScore));
    }
  }

  private int frameCoordinate(int frame) {
    return frame < 9 ? frame * 2 + 1 : frame * 2 + 2;
  }

  @SuppressWarnings("unchecked")
  private void rollBalls(List<Object> table) {
    List<String> rollRow = (List<String>)table.get(0);
    for (int frame = 0; frame < 10; frame++) {
      String firstRoll = rollRow.get(frame * 2);
      String secondRoll = rollRow.get(frame * 2 + 1);
      rollFrame(firstRoll, secondRoll);
    }
  }

  private void rollFrame(String firstRoll, String secondRoll) {
    if (firstRoll.equalsIgnoreCase("X"))
      g.roll(10);
    else {
      int firstRollInt = parseFirstRoll(firstRoll);
      parseSecondRoll(secondRoll, firstRollInt);
    }
  }

  private void parseSecondRoll(String secondRoll, int firstRollInt) {
    if (secondRoll.equals("/"))
      g.roll(10 - firstRollInt);
    else if (secondRoll.equals("-"))
      g.roll(0);
    else
      g.roll(Integer.parseInt(secondRoll));
  }

  private int parseFirstRoll(String firstRoll) {
    int firstRollInt = 0;
    if (firstRoll.equals("-"))
      g.roll(0);
    else {
      firstRollInt = Integer.parseInt(firstRoll);
      g.roll(firstRollInt);
    }
    return firstRollInt;
  }

  private class Game {
    int rolls[] = new int[21];
    int currentRoll = 0;

    public void roll(int pins) {
      rolls[currentRoll++] = pins;
    }

    public int score(int frame) {
      return new Scorer(frame).score();
    }

    private class Scorer {
      private int frame;
      private int score;
      private int firstBall;

      public Scorer(int frame) {
        this.frame = frame;
        score = 0;
        firstBall = 0;
      }

      public int score() {
        for (int f = 0; f < frame; f++)
          scoreFrame();
      
        return score;
      }

      private void scoreFrame() {
        if (isStrike(firstBall)) {
          scoreStrike();
        } else if (isSpare(firstBall)) {
          scoreSpare();
        } else {
          scoreNoMark();
        }
      }

      private void scoreNoMark() {
        score += twoBallsInFrame(firstBall);
        firstBall += 2;
      }

      private void scoreSpare() {
        score += 10 + nextBallForSpare(firstBall);
        firstBall += 2;
      }

      private void scoreStrike() {
        score += 10 + nextTwoBallsForStrike(firstBall);
        firstBall += 1;
      }

      private int twoBallsInFrame(int firstBall) {
        return rolls[firstBall] + rolls[firstBall + 1];
      }

      private int nextBallForSpare(int firstBall) {
        return rolls[firstBall + 2];
      }

      private int nextTwoBallsForStrike(int firstBall) {
        return rolls[firstBall + 1] + rolls[firstBall + 2];
      }

      private boolean isSpare(int firstBall) {
        return rolls[firstBall] + rolls[firstBall + 1] == 10;
      }

      private boolean isStrike(int firstBall) {
        return rolls[firstBall] == 10;
      }
    }
  }
}
