// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fitnesse.acceptanceTests
{
	public class BowlingGame
	{
		private int[] rolls;
		private int currentRoll;

		public BowlingGame()
		{
			rolls = new int[21];
			currentRoll = 0;
		}

		public int GetScore()
		{
			int result = 0;
			int currentRoll = 0;
			for (int frame = 0; frame < 10; frame++)
			{
				if (isStrike(currentRoll))
				{
					result += 10 + nextTwoRollsForStrike(currentRoll);
					currentRoll++;
				}
				else if (isSpare(currentRoll))
				{
					result = result + 10 + nextRollForSpare(currentRoll);
					currentRoll = currentRoll + 2;
				}
				else
				{
					result += rollsForCurrentFrame(currentRoll);
					currentRoll = currentRoll + 2;
				}
			}
			return result;
		}

		private int rollsForCurrentFrame(int currentRoll)
		{
			return rolls[currentRoll] + rolls[currentRoll + 1];
		}

		private int nextRollForSpare(int currentRoll)
		{
			return rolls[currentRoll + 2];
		}

		private int nextTwoRollsForStrike(int currentRoll)
		{
			int i;
			i = rolls[currentRoll + 1] + rolls[currentRoll + 2];
			return i;
		}

		private bool isSpare(int current)
		{
			return rolls[current] + rolls[current + 1] == 10;
		}

		private bool isStrike(int current)
		{
			return rolls[current] == 10;
		}

		public void Roll(int pins)
		{
			rolls[currentRoll++] = pins;
		}
	}
}
