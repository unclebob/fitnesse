// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;

namespace fitnesse.acceptanceTests
{
	public class BowlingGameFixture : TableFixture
	{
		protected override void DoStaticTable(int rows)
		{
			for (int row = 0; row < rows; row++)
			{
				BowlingGame game = new BowlingGame();
				for (int column = 0; column < 21; column++)
				{
					if (!Blank(row, column))
					{
						game.Roll(GetInt(row, column));
					}
				}
				int expectedScore = GetInt(row, 21);
				if (game.GetScore() == expectedScore)
				{
					Right(row, 21);
				}
				else
				{
					Wrong(row, 21, game.GetScore().ToString());
				}
			}
		}
	}
}