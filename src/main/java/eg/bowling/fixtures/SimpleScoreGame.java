// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling.fixtures;

import eg.bowling.Bowling;
import eg.bowling.BowlingGame;
import fitnesse.fixtures.TableFixture;

public class SimpleScoreGame extends TableFixture {
  private Bowling game;

  protected void doStaticTable(int rows) {
    game = new BowlingGame();
    doRolls();
    doScores();
  }

  private void doRolls() {
    for (int i = 0; i < 21; i++) {
      if (!blank(0, i)) {
        int pins = getInt(0, i);
        game.roll(pins);
      }
    }
  }

  private void doScores() {
    for (int frame = 1; frame <= 10; frame++) {
      int column = frame - 1;
      int expected = getInt(1, column);
      int actual = game.score(frame);
      if (actual == expected)
        right(1, column);
      else
        wrong(1, column, "" + actual);
    }
  }
}
