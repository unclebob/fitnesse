// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class BlankKeywordHandlerTest
	{
		private Parse cell;

		[SetUp]
		public void SetUp()
		{
			cell = CellHandlerTestUtils.CreateCell("blank");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new BlankKeywordHandler());
		}

		[Test]
		public void TestRegisterAndGetBlankKeywordOperator()
		{
			Assert.IsTrue(CellOperation.GetHandler("blank", null) is BlankKeywordHandler);
			Assert.IsFalse(CellOperation.GetHandler("is blank", null) is BlankKeywordHandler);
		}

		[Test]
		public void TestDoInputBlank()
		{
			StringFixture fixture = new StringFixture();
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual("", fixture.Field);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckBlankRight() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "";
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual("", fixture.Field);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.AssertValueInBody(cell, "blank");
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoEvaluateBlankRight() {
			StringFixture fixture = new StringFixture();
			fixture.Field = "";
			Assert.IsTrue(CellOperation.Evaluate(fixture, "Field", cell));
			Assert.AreEqual("", fixture.Field);
			CellHandlerTestUtils.AssertValueInBody(cell, "blank");
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckBlankWrongValue()
		{
			StringFixture fixture = new StringFixture();
			fixture.Field = "some value";
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual("some value", fixture.Field);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"blank", "some value"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestDoCheckBlankNullValue()
		{
			StringFixture fixture = new StringFixture();
			fixture.Field = null;
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual(null, fixture.Field);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"blank", "null"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestDoCheckBlankWrongTypeRightValue()
		{
			PersonFixture fixture = new PersonFixture();
			fixture.Field = new Person("", "");
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual("", fixture.Field.ToString());
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"blank"});
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckBlankWrongTypeWrongValue() {
			PersonFixture fixture = new PersonFixture();
			fixture.Field = new Person("john", "doe");
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual("john doe", fixture.Field.ToString());
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"blank", "john doe"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}
	}
}