// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class ScientificDoubleTest
	{
		[Test]
		public void TestScientificDouble()
		{
			double pi = 3.141592865;
			Assert.AreEqual(ScientificDouble.ValueOf("3.14"), pi);
			Assert.AreEqual(ScientificDouble.ValueOf("3.142"), pi);
			Assert.AreEqual(ScientificDouble.ValueOf("3.1416"), pi);
			Assert.AreEqual(ScientificDouble.ValueOf("3.14159"), pi);
			Assert.AreEqual(ScientificDouble.ValueOf("3.141592865"), pi);
			Assert.IsTrue(!ScientificDouble.ValueOf("3.140").Equals(pi));
			Assert.IsTrue(!ScientificDouble.ValueOf("3.144").Equals(pi));
			Assert.IsTrue(!ScientificDouble.ValueOf("3.1414").Equals(pi));
			Assert.IsTrue(!ScientificDouble.ValueOf("3.141592863").Equals(pi));
			Assert.AreEqual(ScientificDouble.ValueOf("6.02e23"), 6.02e23d);
			Assert.AreEqual(ScientificDouble.ValueOf("6.02E23"), 6.024E23d);
			Assert.AreEqual(ScientificDouble.ValueOf("6.02e23"), 6.016e23d);
			Assert.IsTrue(!ScientificDouble.ValueOf("6.02e23").Equals(6.026e23d));
			Assert.IsTrue(!ScientificDouble.ValueOf("6.02e23").Equals(6.014e23d));
			Assert.AreEqual(ScientificDouble.ValueOf("3.14"), ScientificDouble.ValueOf("3.14"));
		}
	}
}
