// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling;

public class BowlingGame implements Bowling {
  private static final int FIRST_BALL_IN_FRAME = 0;
  private static final int SECOND_BALL_IN_FRAME = 1;
  private static final int GAME_OVER = 2;
  private static final int BALL_AFTER_SPARE = 3;
  private static final int BALL_AFTER_TENTH_FRAME_SPARE = 4;
  private static final int BALL_AFTER_FIRST_STRIKE = 5;
  private static final int BALL_AFTER_SECOND_STRIKE = 6;

  private int currentFrame = 1;
  private int currentBall = 1;
  private int scoreableFrame = 0;
  private boolean gameOver = false;

  private int state = FIRST_BALL_IN_FRAME;
  private final BowlingScorer bowlingScorer = new BowlingScorer();

  private void changeState() {
    switch (state) {
      case FIRST_BALL_IN_FRAME:
        firstBallInFrame();
        break;

      case SECOND_BALL_IN_FRAME:
        secondBallInFrame();
        break;

      case BALL_AFTER_SPARE:
        ballAfterSpare();
        break;

      case BALL_AFTER_TENTH_FRAME_SPARE:
        endGame();
        break;

      case BALL_AFTER_FIRST_STRIKE:
        ballAfterFirstStrike();
        break;

      case BALL_AFTER_SECOND_STRIKE:
        ballAfterSecondStrike();
        break;
    }
  }

  private void firstBallInFrame() {
    if (bowlingScorer.lastRollWasStrike()) {
      state = BALL_AFTER_FIRST_STRIKE;
      incrementFrame();
    } else {
      state = SECOND_BALL_IN_FRAME;
      currentBall = 2;
    }
  }

  private void secondBallInFrame() {
    if (bowlingScorer.lastRollWasSpare() && currentFrame == 10) {
      state = BALL_AFTER_TENTH_FRAME_SPARE;
      currentBall = 3;
    } else if (bowlingScorer.lastRollWasSpare()) {
      state = BALL_AFTER_SPARE;
      incrementFrame();
    } else if (currentFrame == 10) {
      endGame();
    } else {
      state = FIRST_BALL_IN_FRAME;
      scoreableFrame = currentFrame;
      incrementFrame();
    }
  }

  private void ballAfterSpare() {
    if (bowlingScorer.lastRollWasStrike()) {
      state = BALL_AFTER_FIRST_STRIKE;
      scoreableFrame++;
      incrementFrame();
    } else {
      state = SECOND_BALL_IN_FRAME;
      currentBall = 2;
      scoreableFrame++;
    }
  }

  private void ballAfterFirstStrike() {
    if (bowlingScorer.lastRollWasStrike()) {
      state = BALL_AFTER_SECOND_STRIKE;
      incrementFrame();
    } else {
      state = SECOND_BALL_IN_FRAME;
      currentBall = 2;
    }
  }

  private void ballAfterSecondStrike() {
    if (bowlingScorer.lastRollWasStrike() && currentFrame == 10 && currentBall == 3) {
      endGame();
    } else if (bowlingScorer.lastRollWasStrike()) {
      incrementFrame();
      scoreableFrame++;
    } else {
      state = SECOND_BALL_IN_FRAME;
      currentBall = 2;
      scoreableFrame++;
    }
  }

  private void endGame() {
    state = GAME_OVER;
    currentBall = 0;
    scoreableFrame = 10;
    gameOver = true;
  }

  private void incrementFrame() {
    if (currentFrame < 10) {
      currentFrame++;
      currentBall = 1;
    } else
      currentBall++;
  }

  public int currentFrame() {
    return currentFrame;
  }

  public int currentBall() {
    return currentBall;
  }

  public int scoreableFrame() {
    return scoreableFrame;
  }

  public boolean validGame() {
    return true;
  }

  public boolean gameOver() {
    return currentFrame == 10 && currentBall == 0;
  }

  public boolean isGameOver() {
    return gameOver;
  }

  public void roll(int pins) {
    bowlingScorer.roll(pins);
    changeState();
  }

  public int score(int frame) {
    return bowlingScorer.score(frame);
  }
}
