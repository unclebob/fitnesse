// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class IntegralRangeHandlerTest
	{
		private Parse cell;

		[SetUp]
		public void SetUp() {
			cell = CellHandlerTestUtils.CreateCell("0..2");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new IntegralRangeHandler());
		}

		[Test]
		public void TestRegisterAndGet() {
			Assert.IsTrue(CellOperation.GetHandler("0..2", null) is DefaultCellHandler);
			Assert.IsFalse(CellOperation.GetHandler("..2", null) is IntegralRangeHandler);
			Assert.IsTrue(CellOperation.GetHandler("0..2", typeof(int)) is IntegralRangeHandler);
			Assert.IsTrue(CellOperation.GetHandler("-10..-2", typeof(int)) is IntegralRangeHandler);
			Assert.IsTrue(CellOperation.GetHandler("-10..23", typeof(int)) is IntegralRangeHandler);
			Assert.IsTrue(CellOperation.GetHandler("12..37", typeof(int)) is IntegralRangeHandler);
			Assert.IsTrue(CellOperation.GetHandler("a..b", typeof(int)) is AbstractCellHandler);
		}

		[Test]
		public void TestInRange() {
			IntFixture fixture = new IntFixture();
			fixture.Field = 1;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestStartOfRange() {
			IntFixture fixture = new IntFixture();
			fixture.Field = 0;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestEndOfRange() {
			IntFixture fixture = new IntFixture();
			fixture.Field = 2;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestNotInRange() {
			IntFixture fixture = new IntFixture();
			fixture.Field = 5;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestNegativeNumbers() {
			cell = CellHandlerTestUtils.CreateCell("-457..-372");
			IntFixture fixture = new IntFixture();
			fixture.Field = -400;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestNegativeLowPositiveHigh() {
			cell = CellHandlerTestUtils.CreateCell("-457..372");
			IntFixture fixture = new IntFixture();
			fixture.Field = 0;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}
	}
}
