// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class BoolHandlerTest
	{
		[SetUp]
		public void SetUp()
		{
			CellOperation.LoadHandler(new BoolHandler());
		}

		[Test]
		public void TestRegisterAndGet()
		{
			string[] searchStrings = new string[]
				{
					"Y", "y",
					"YES", "yes", "YeS", "yEs",
					"true", "TRUE", "TrUe", "tRuE",
					"N", "n",
					"NO", "no", "No", "nO",
					"false", "FALSE", "fAlSe", "FaLsE"
				};
			foreach (string searchString in searchStrings)
			{
				AssertFound(searchString);
			}
		}

		private void AssertFound(string searchString)
		{
			Assert.IsTrue(CellOperation.GetHandler(searchString, null) is BoolHandler);
		}

		[Test]
		public void TestHandleInputForBoolTypeTrue()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("Y");
			BoolFixture fixture = new BoolFixture();
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual(true, fixture.Field);
			CellHandlerTestUtils.AssertValueInBody(cell, "Y");
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestHandleInputForBoolTypeFalse()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("N");
			BoolFixture fixture = new BoolFixture();
			fixture.Field = true;
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual(false, fixture.Field);
			CellHandlerTestUtils.AssertValueInBody(cell, "N");
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestHandleCheckForBoolTypeTruePass()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("Y");
			BoolFixture fixture = new BoolFixture();
			fixture.Field = true;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.AssertValueInBody(cell, "Y");
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestHandleCheckForBoolTypeFalsePass()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("no");
			BoolFixture fixture = new BoolFixture();
			fixture.Field = false;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.AssertValueInBody(cell, "no");
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestHandleCheckForBoolTypeTrueFail()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("No");
			BoolFixture fixture = new BoolFixture();
			fixture.Field = true;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"True", "No"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestHandleCheckForBoolTypeFalseFail()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("yEs");
			BoolFixture fixture = new BoolFixture();
			fixture.Field = false;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"False", "yEs"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestHandleInputForString()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("Y");
			StringFixture fixture = new StringFixture();
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual("Y", fixture.Field);
			CellHandlerTestUtils.AssertValueInBody(cell, "Y");
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestHandleCheckForStringPass()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("Y");
			StringFixture fixture = new StringFixture();
			fixture.Field = "Y";
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellPasses(cell);
			CellHandlerTestUtils.AssertValueInBody(cell, "Y");
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestHandleCheckForStringFail() {
			Parse cell = CellHandlerTestUtils.CreateCell("Y");
			StringFixture fixture = new StringFixture();
			fixture.Field = "Yes";
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.AssertCellFails(cell);
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"Y", "Yes"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestHandleEvaluateForStringFail() {
			Parse cell = CellHandlerTestUtils.CreateCell("Y");
			StringFixture fixture = new StringFixture();
			fixture.Field = "Yes";
			Assert.IsFalse(CellOperation.Evaluate(fixture, "Field", cell));
			CellHandlerTestUtils.AssertValuesInBody(cell, new string[] {"Y"});
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}
	}
}