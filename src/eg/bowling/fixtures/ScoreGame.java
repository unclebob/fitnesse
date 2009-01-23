// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling.fixtures;

import eg.bowling.Bowling;
import eg.bowling.BowlingGame;
import fitnesse.fixtures.TableFixture;

public class ScoreGame extends TableFixture {
  private Bowling game;

  public static final int STRIKE = -1;
  public static final int SPARE = -2;
  public static final int BLANK = -3;
  public static final int ERROR = -4;

  protected void doStaticTable(int rows) {
    game = new BowlingGame();
    doRolls();
    doScores();
  }

  private void doRolls() {
    for (int roll = 0; roll < 21; roll++)
      doRoll(roll);
  }

  private void doRoll(int roll) {
    int pins = parseRoll(roll);
    if (pins == SPARE)
      spare(roll);
    else if (pins == STRIKE)
      strike(roll);
    else if (pins == BLANK)
      blank(roll);
    else if (pins == ERROR)
      wrongRoll(roll);
    else {
      roll(pins);
    }
  }

  private int parseRoll(int roll) {
    String rollText = getText(1, roll);
    if (rollText.equals("/"))
      return SPARE;
    else if (rollText.equals("X"))
      return STRIKE;
    else if (rollText.equals(""))
      return BLANK;
    else {
      try {
        int pins = Integer.parseInt(rollText);
        return pins;
      }
      catch (NumberFormatException e) {
        return ERROR;
      }
    }
  }

  private void spare(int rollNumber) {
    if (odd(rollNumber))
      wrongRoll(rollNumber);
    else {
      int previousRoll = parseRoll(rollNumber - 1);
      if (previousRoll < 0)
        wrongRoll(rollNumber);
      else
        roll(10 - previousRoll);
    }
  }

  private void wrongRoll(int rollNumber) {
    wrong(1, rollNumber);
  }

  private void strike(int rollNumber) {
    if (tenthFrameStrike(rollNumber)) {
      roll(10);
      return;
    }
    if (odd(rollNumber)) {
      wrongRoll(rollNumber);
      return;
    }
    int previousRoll = parseRoll(rollNumber - 1);
    if (previousRoll != BLANK)
      wrongRoll(rollNumber);
    else
      roll(10);

  }

  private boolean tenthFrameStrike(int rollNumber) {
    return rollNumber == 18 ||
      (rollNumber == 19 && parseRoll(18) == STRIKE) ||
      (rollNumber == 20 && (parseRoll(19) == STRIKE || parseRoll(19) == SPARE));
  }

  private boolean odd(int rollNumber) {
    return (rollNumber % 2) != 1;
  }

  private void blank(int roll) {
    if (roll == 20 && parseRoll(19) != SPARE &&
      parseRoll(18) != STRIKE)
      return;
    if (parseRoll(roll + 1) == STRIKE)
      return;
    wrongRoll(roll);
  }

  private void roll(int pins) {
    game.roll(pins);
  }

  private void doScores() {
    for (int frame = 0; frame < 10; frame++)
      scoreFrame(frame);
  }

  private void scoreFrame(int frame) {
    int expectedScore = getScore(frame);
    int actualScore = game.score(frame + 1);
    if (expectedScore == actualScore)
      rightScore(frame);
    else
      wrongScore(frame, "" + actualScore);
  }

  private void rightScore(int frame) {
    right(2, scoreIndex(frame));
  }

  private void wrongScore(int frame, String actual) {
    wrong(2, scoreIndex(frame), actual);
  }

  private int getScore(int frame) {
    return Integer.parseInt(getText(2, scoreIndex(frame)));
  }

  private int scoreIndex(int frame) {
    return frame * 2 + 1;
  }
}
