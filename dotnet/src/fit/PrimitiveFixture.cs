// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

namespace fit
{
	public class PrimitiveFixture : Fixture
	{
		// format converters ////////////////////////
		public static long ParseLong(Parse cell) 
		{
			return long.Parse(cell.Text);
		}

		public static double ParseDouble (Parse cell) 
		{
			return double.Parse(cell.Text);
		}

		// answer comparisons ///////////////////////

		public virtual void Check(Parse cell, string actual) 
		{
			if (cell.Text == actual) 
				Right(cell);
			else 
				Wrong(cell, actual);
		}

		public virtual void Check(Parse cell, long actual) 
		{
			if (ParseLong(cell) == actual) 
				Right(cell);
			else 
				Wrong(cell, actual.ToString());
		}

		public virtual void Check(Parse cell, double actual) 
		{
			if (ParseDouble(cell) == actual) 
				Right(cell);
			else 
				Wrong(cell, actual.ToString());
		}
	}
}