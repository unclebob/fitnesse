// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class CountsTest
	{
		private Counts counts;

		[SetUp]
		public void SetUp()
		{
			counts = new Counts();
		}
		
		[Test]
		public void TestInitialState()
		{
			Assert.AreEqual(0, counts.Right);
			Assert.AreEqual(0, counts.Wrong);
			Assert.AreEqual(0, counts.Exceptions);
			Assert.AreEqual(0, counts.Ignores);
		}

		[Test]
		public void TestTally()
		{
			SetCounts(1,2,3,4);
			counts.Tally(counts);
			Assert.AreEqual(2, counts.Right);
			Assert.AreEqual(4, counts.Wrong);
			Assert.AreEqual(6, counts.Exceptions);
			Assert.AreEqual(8, counts.Ignores);
		}

		[Test]
		public void TestProperties()
		{
			SetCounts(9,8,7,6);
			Assert.AreEqual(9, counts.Right);
			Assert.AreEqual(8, counts.Wrong);
			Assert.AreEqual(7, counts.Exceptions);
			Assert.AreEqual(6, counts.Ignores);
		}

		private void SetCounts(int right, int wrong, int exceptions, int ignores)
		{
			counts.Right = right;
			counts.Wrong = wrong;
			counts.Exceptions = exceptions;
			counts.Ignores = ignores;
		}
	}
}
