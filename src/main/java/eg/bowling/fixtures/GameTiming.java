// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling.fixtures;

import eg.bowling.Bowling;
import eg.bowling.BowlingGame;
import fit.ColumnFixture;

public class GameTiming extends ColumnFixture {
  private Bowling game;
  public String pins;

  public GameTiming() {
    super();
    game = new BowlingGame();
  }

  public boolean roll() {
    if (pins.equals("-"))
      return false;
    else {
      game.roll(Integer.parseInt(pins));
      return true;
    }
  }

  public int currentFrame() {
    return game.currentFrame();
  }

  public int currentBall() {
    return game.currentBall();
  }

  public int scorableFrame() {
    return game.scoreableFrame();
  }

  public int currentScore() {
    return game.score(game.scoreableFrame());
  }

  public boolean validGame() {
    return game.validGame();
  }

  public boolean gameOver() {
    return game.gameOver();
  }

}
