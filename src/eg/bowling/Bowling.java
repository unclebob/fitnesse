// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling;

public interface Bowling {
  int currentFrame();

  int currentBall();

  int scoreableFrame();

  boolean validGame();

  boolean gameOver();

  boolean isGameOver();

  void roll(int pins);

  int score(int frame);
}
