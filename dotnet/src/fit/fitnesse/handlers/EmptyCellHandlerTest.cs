// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class EmptyCellHandlerTest
	{
		private Parse cell;

		[SetUp]
		public void SetUp() {
			cell = CellHandlerTestUtils.CreateCell("");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new EmptyCellHandler());
		}

		[Test]
		public void TestRegisterAndGetEmptyCellOperator() {
			Assert.IsTrue(CellOperation.GetHandler("", null) is EmptyCellHandler);
			Assert.IsTrue(CellOperation.GetHandler("", null) is EmptyCellHandler);
		}

		[Test]
		public void TestInputWhereNullValueExists() {
			StringFixture fixture = new StringFixture();
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual(null, fixture.Field);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"fit_grey", "null"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestInputWhereBlankValueExists() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "";
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual("", fixture.Field);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"fit_grey", "blank"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestInputWhereValueExists() {
			IntFixture fixture = new IntFixture();
			fixture.Field = 37;
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual(37, fixture.Field);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"fit_grey", "37"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestInputNullValueWithMethod() {
			StringFixture fixture = new StringFixture();
			CellOperation.Input(fixture, "Set", cell);
			Assert.AreEqual(null, fixture.Field);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestCheckNullValue() {
			StringFixture fixture = new StringFixture();
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"fit_grey", "null"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestCheckBlankValue() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "";
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"fit_grey", "blank"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestCheckNonNullNonBlankValue() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "a value";
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"fit_grey", "a value"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}
	}
}