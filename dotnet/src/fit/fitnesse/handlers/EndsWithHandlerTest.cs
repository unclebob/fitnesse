// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class EndsWithHandlerTest
	{
		private Parse cell;

		[SetUp]
		public void SetUp()
		{
			cell = CellHandlerTestUtils.CreateCell("abc..");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new SubstringHandler());
			CellOperation.LoadHandler(new EndsWithHandler());
		}

		[Test]
		public void TestRegisterAndGet()
		{
			Assert.IsTrue(CellOperation.GetHandler("abc..", null) is AbstractCellHandler);
			Assert.IsTrue(CellOperation.GetHandler("..abc", null) is EndsWithHandler);
			Assert.IsTrue(CellOperation.GetHandler("..abc..", null) is AbstractCellHandler);
		}

		[Test]
		public void TestPass()
		{
			StringFixture fixture = new StringFixture();
			fixture.Field = "abcde";
			cell = CellHandlerTestUtils.CreateCell("..cde");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestFailWhereSubstringExists() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "abcde";
			cell = CellHandlerTestUtils.CreateCell("..abc");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestFailNull() {
			StringFixture fixture = new StringFixture();
			fixture.Field = null;
			cell = CellHandlerTestUtils.CreateCell("..abc");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

	}
}