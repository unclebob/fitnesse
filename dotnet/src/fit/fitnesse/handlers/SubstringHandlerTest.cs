// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class SubstringHandlerTest
	{
		private Parse cell;

		[SetUp]
		public void SetUp()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new SubstringHandler());
		}

		[Test]
		public void TestRegisterAndGet()
		{
			Assert.IsTrue(CellOperation.GetHandler("..text..", null) is SubstringHandler);
			Assert.IsFalse(CellOperation.GetHandler("text..", null) is SubstringHandler);
			Assert.IsFalse(CellOperation.GetHandler("..text", null) is SubstringHandler);
		}

		[Test]
		public void TestPass() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "abcde";
			cell = CellHandlerTestUtils.CreateCell("..bcd..");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestStartsWithPass() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "abcde";
			cell = CellHandlerTestUtils.CreateCell("..abc..");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestEndsWithPass() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "abcde";
			cell = CellHandlerTestUtils.CreateCell("..cde..");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestFails() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "abcde";
			cell = CellHandlerTestUtils.CreateCell("..bce..");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestFailNull() {
			StringFixture fixture = new StringFixture();
			fixture.Field = null;
			cell = CellHandlerTestUtils.CreateCell("..bce..");
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValueInBody(cell, "null");
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}
	}
}