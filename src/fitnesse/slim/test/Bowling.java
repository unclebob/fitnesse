package fitnesse.slim.test;

import static fitnesse.util.ListUtility.*;

import java.util.List;

public class Bowling {
  public List doTable(List<List<String>> table) {
    Game g = new Game();
    List rollResults = list("","","","","","","","","","","","","","","","","","","","","");
    List scoreResults = list("","","","","","","","","","","","","","","","","","","","","");
    rollBalls(table, g);
    evaluateScores(g, table.get(1), scoreResults);
    return list(rollResults, scoreResults);
  }

  private void evaluateScores(Game g, List<String> scoreRow, List scoreResults) {
    for (int frame=0; frame<10; frame++) {
      int actualScore = g.score(frame+1);
      int expectedScore = Integer.parseInt(scoreRow.get(frameCoordinate(frame)));
      if (expectedScore == actualScore)
        scoreResults.set(frameCoordinate(frame), "pass");
      else
        scoreResults.set(frameCoordinate(frame), String.format("Was:%d, expected:%s.", actualScore, expectedScore));
    }
  }

  private int frameCoordinate(int frame) {
    return frame < 9 ? frame*2+1 : frame*2+2;
  }

  private void rollBalls(List<List<String>> table, Game g) {
    List<String> rollRow = table.get(0);
    for (int frame = 0; frame < 10; frame++) {
      String firstRoll = rollRow.get(frame * 2);
      String secondRoll = rollRow.get(frame * 2 + 1);
      if (firstRoll.equalsIgnoreCase("X"))
        g.roll(10);
      else {
        int firstRollInt = 0;
        if (firstRoll.equals("-"))
          g.roll(0);
        else {
          firstRollInt = Integer.parseInt(firstRoll);
          g.roll(firstRollInt);
        }
        if (secondRoll.equals("/"))
          g.roll(10 - firstRollInt);
        else if (secondRoll.equals("-"))
          g.roll(0);
        else
          g.roll(Integer.parseInt(secondRoll));
      }
    }
  }

  private class Game {
    int rolls[] = new int[21];
    int currentRoll = 0;

    public void roll(int pins) {
      rolls[currentRoll++] = pins;
    }

    public int score(int frame) {
      int score = 0;
      int firstBall = 0;
      for (int f=0; f<frame; f++) {
        if (isStrike(firstBall)) {
          score += 10 + nextTwoBallsForStrike(firstBall);
          firstBall += 1;
        } else if (isSpare(firstBall)) {
          score += 10 + nextBallForSpare(firstBall);
          firstBall += 2;
        } else {
          score += twoBallsInFrame(firstBall);
          firstBall += 2;
        }
      }
      return score;
    }

    private int twoBallsInFrame(int firstBall) {
      return rolls[firstBall] + rolls[firstBall+1];
    }

    private int nextBallForSpare(int firstBall) {
      return rolls[firstBall+2];
    }

    private int nextTwoBallsForStrike(int firstBall) {
      return rolls[firstBall+1] + rolls[firstBall+2];
    }

    private boolean isSpare(int firstBall) {
      return rolls[firstBall] + rolls[firstBall+1] == 10;
    }

    private boolean isStrike(int firstBall) {
      return rolls[firstBall] == 10;
    }
  }
}
