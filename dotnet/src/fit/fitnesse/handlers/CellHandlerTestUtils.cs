// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	public class CellHandlerTestUtils
	{
		public static void AssertCellFails(Parse cell)
		{
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
		}

		public static void AssertValueInBody(Parse cell, string value)
		{
			Assert.IsTrue(cell.Body.IndexOf(value) > -1);
		}

		public static void AssertValuesInBody(Parse cell, string[] values)
		{
			foreach (string value in values)
			{
				AssertValueInBody(cell, value);
			}
		}

		public static void AssertCellPasses(Parse cell)
		{
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
		}

		public static void VerifyCounts(Fixture fixture, int right, int wrong, int ignores, int exceptions)
		{
			Assert.AreEqual(right, fixture.Counts.Right);
			Assert.AreEqual(wrong, fixture.Counts.Wrong);
			Assert.AreEqual(ignores, fixture.Counts.Ignores);
			Assert.AreEqual(exceptions, fixture.Counts.Exceptions);
		}

		public static Parse CreateCell(string value)
		{
			Parse cell;
			Parse table = new Parse("<table><tr><td>" + value + "</td></tr></table>");
			cell = table.Parts.Parts;
			return cell;
		}
	}
}