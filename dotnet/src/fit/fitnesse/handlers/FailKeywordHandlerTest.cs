// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class FailKeywordHandlerTest
	{
		[Test]
		public void TestRegisterAndGetsErrorKeywordOperator()
		{
			Assert.IsTrue(CellOperation.GetHandler("fail[]", null) is FailKeywordHandler);
			Assert.IsTrue(CellOperation.GetHandler("fail[some value]", null) is FailKeywordHandler);
		}

		[Test]
		public void TestFailInt()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("fail[1]");
			IntFixture fixture = new IntFixture();
			fixture.Field = 2;
			CellOperation.Check(fixture, "field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestFailOnCorrectInt()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("fail[2]");
			IntFixture fixture = new IntFixture();
			fixture.Field = 2;
			CellOperation.Check(fixture, "field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestFailString()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("fail[some string]");
			StringFixture fixture = new StringFixture();
			fixture.Field = "some other string";
			CellOperation.Check(fixture, "field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestFailPerson()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("fail[Doctor Jeckyll]");
			PersonFixture fixture = new PersonFixture();
			fixture.Field = new Person("Mister", "Hyde");
			CellOperation.Check(fixture, "field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}
	}
}