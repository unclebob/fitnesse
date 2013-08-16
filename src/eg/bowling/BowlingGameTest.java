// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.bowling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BowlingGameTest {
  private Bowling g;

  @Before
  public void setUp() throws Exception {
    g = new BowlingGame();
  }

  @Test
  public void testGutterGame() throws Exception {
    rollMany(20, 0);
    assertEquals(0, g.score(10));
  }

  @Test
  public void testAllOnes() throws Exception {
    rollMany(20, 1);
    assertEquals(20, g.score(10));
  }

  @Test
  public void testSpare() throws Exception {
    g.roll(5);
    g.roll(5); // spare
    g.roll(3);
    rollMany(17, 0);
    assertEquals(16, g.score(10));
  }

  @Test
  public void testStrike() throws Exception {
    g.roll(10); // strike
    g.roll(3);
    g.roll(5);
    rollMany(16, 0);
    assertEquals(26, g.score(10));
  }

  @Test
  public void testPerfectGame() throws Exception {
    rollMany(12, 10);
    assertEquals(300, g.score(10));
  }

  private void rollMany(int n, int pins) {
    for (int i = 0; i < n; i++)
      g.roll(pins);
  }

  @Test
  public void testCurrentFrameStartsAtOne() throws Exception {
    assertEquals(1, g.currentFrame());
  }

  @Test
  public void testCurrentFrameNoStrike() throws Exception {
    g.roll(1);
    assertEquals(1, g.currentFrame());
    g.roll(2);
    assertEquals(2, g.currentFrame());
  }

  @Test
  public void testCurrentFrameNeverExceedsTen() throws Exception {
    rollMany(20, 0);
    assertEquals(10, g.currentFrame());
  }

  @Test
  public void testCurrentBallStartsAtOne() throws Exception {
    assertEquals(1, g.currentBall());
  }

  @Test
  public void testCurrentBallNoStrike() throws Exception {
    g.roll(1);
    assertEquals(2, g.currentBall());
    g.roll(1);
    assertEquals(1, g.currentBall());
  }

  @Test
  public void testCurrentBallZeroWhenGameOver() throws Exception {
    rollMany(20, 0);
    assertEquals(0, g.currentBall());
  }

  @Test
  public void testScoreableFrameStartsAtZero() throws Exception {
    assertEquals(0, g.scoreableFrame());
  }

  @Test
  public void testScoreableFrameNoMarks() throws Exception {
    g.roll(1);
    assertEquals(0, g.scoreableFrame());
    g.roll(1);
    assertEquals(1, g.scoreableFrame());
    rollMany(18, 1);
    assertEquals(10, g.scoreableFrame());
  }

  @Test
  public void testScoreableFrameForSpare() throws Exception {
    g.roll(5);
    g.roll(5);
    assertEquals(0, g.scoreableFrame());
    g.roll(3);
    assertEquals(1, g.scoreableFrame());
    g.roll(4);
    assertEquals(2, g.scoreableFrame());
  }

  @Test
  public void testScoreableFrameForTenthFrameSpare() throws Exception {
    rollMany(18, 0);
    g.roll(5);
    g.roll(5);  //tenth frame spare
    assertEquals(10, g.currentFrame());
    assertEquals(3, g.currentBall());
    assertEquals(9, g.scoreableFrame());
    assertEquals(false, g.gameOver());
    g.roll(5); // last ball;
    assertEquals(10, g.currentFrame());
    assertEquals(0, g.currentBall());
    assertEquals(10, g.scoreableFrame());
    assertTrue(g.gameOver());
  }

  @Test
  public void testGameNotOver() throws Exception {
    assertEquals(false, g.gameOver());
  }

  @Test
  public void testGameOverNoMarks() throws Exception {
    for (int i = 0; i < 20; i++) {
      assertEquals(false, g.gameOver());
      g.roll(0);
    }
    assertEquals(true, g.gameOver());
  }

  @Test
  public void testCurrentFrameForStrike() throws Exception {
    g.roll(10); // strike
    assertEquals(2, g.currentFrame());
    assertEquals(1, g.currentBall());
    assertEquals(0, g.scoreableFrame());
    g.roll(3);
    assertEquals(2, g.currentFrame());
    assertEquals(2, g.currentBall());
    assertEquals(0, g.scoreableFrame());
    g.roll(3);
    assertEquals(3, g.currentFrame());
    assertEquals(1, g.currentBall());
    assertEquals(2, g.scoreableFrame());
  }

  @Test
  public void testStrikeAfterSpare() throws Exception {
    g.roll(5);
    g.roll(5);
    g.roll(10);
    assertEquals(3, g.currentFrame());
    assertEquals(1, g.currentBall());
    assertEquals(1, g.scoreableFrame());
  }

  @Test
  public void testManyStrikes() throws Exception {
    g.roll(10);
    g.roll(10);
    assertEquals(3, g.currentFrame());
    assertEquals(1, g.currentBall());
    assertEquals(0, g.scoreableFrame());
    rollMany(7, 10); // ninth frame
    assertEquals(10, g.currentFrame());
    assertEquals(1, g.currentBall());
    assertEquals(7, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 2, g.currentBall());
    assertEquals("", 8, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 3, g.currentBall());
    assertEquals("", 9, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 0, g.currentBall());
    assertEquals("", 10, g.scoreableFrame());
    assertTrue(g.gameOver());
  }

  @Test
  public void testFinalStrike() throws Exception {
    rollMany(18, 0);
    g.roll(5);
    g.roll(5);
    g.roll(10);
    assertEquals("", true, g.gameOver());
    assertEquals("", 0, g.currentBall());
    assertEquals("", 10, g.scoreableFrame());
  }

  @Test
  public void testFourFinalStrikes() throws Exception {
    rollMany(16, 0);
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 1, g.currentBall());
    assertEquals("", 8, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 2, g.currentBall());
    assertEquals("", 8, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 3, g.currentBall());
    assertEquals("", 9, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 0, g.currentBall());
    assertEquals("", 10, g.scoreableFrame());
    assertEquals("", true, g.gameOver());
  }

  @Test
  public void testThreeFinalStrikes() throws Exception {
    rollMany(18, 0);
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 2, g.currentBall());
    assertEquals("", 9, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 3, g.currentBall());
    assertEquals("", 9, g.scoreableFrame());
    g.roll(10);
    assertEquals("", 10, g.currentFrame());
    assertEquals("", 0, g.currentBall());
    assertEquals("", 10, g.scoreableFrame());
    assertEquals("", true, g.gameOver());
  }
}
