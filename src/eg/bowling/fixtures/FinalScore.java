// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling.fixtures;

import eg.bowling.Bowling;
import eg.bowling.BowlingGame;
import fitnesse.fixtures.TableFixture;

public class FinalScore extends TableFixture {
  private Bowling game;

  protected void doStaticTable(int rows) {
    game = new BowlingGame();
    doRolls();
    doScore();
  }

  private void doRolls() {
    for (int i = 0; i < 21; i++) {
      if (!blank(0, i)) {
        int pins = getInt(0, i);
        game.roll(pins);
      }
    }
  }

  private void doScore() {
    int expected = getInt(0, 21);
    int actual = game.score(10);
    if (actual == expected)
      right(0, 21);
    else
      wrong(0, 21, "" + actual);
  }
}

