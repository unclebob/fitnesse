// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using NUnit.Framework;

namespace fitnesse.acceptanceTests
{
	[TestFixture]
	public class BowlingGameTest
	{
		private BowlingGame game;

		[SetUp]
		public void SetUp()
		{
			game = new BowlingGame();
		}

		[Test]
		public void TestGutterGame()
		{
			RollMany(20, 0);
			Assert.AreEqual(0, game.GetScore());
		}

		[Test]
		public void TestOnes()
		{
			RollMany(20, 1);
			Assert.AreEqual(20, game.GetScore());
		}
	
		[Test]
		public void TestFives()
		{
			RollMany(21, 5);
			Assert.AreEqual(150, game.GetScore());
		}

		[Test]
		public void TestPerfectGame()
		{
			RollMany(12, 10);
			Assert.AreEqual(300, game.GetScore());
		}

		private void RollMany(int rolls, int pins)
		{
			for (int i = 0; i < rolls; i++)
			{
				game.Roll(pins);
			}
		}
	}
}
