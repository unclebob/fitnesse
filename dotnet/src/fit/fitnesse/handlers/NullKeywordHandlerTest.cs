// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class NullKeywordHandlerTest
	{	
		[Test]
		public void TestRegisterAndGetsNullKeywordOperator()
		{
			CellOperation.LoadHandler(new NullKeywordHandler());
			Assert.IsTrue(CellOperation.GetHandler("null", null) is NullKeywordHandler);
		}

		[Test]
		public void TestDoInputNull()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new NullKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("null");
			StringFixture fixture = new StringFixture();
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual(null, fixture.Field);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckNullRight()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new NullKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("null");
			StringFixture fixture = new StringFixture();
			fixture.Field = null;
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual(null, fixture.Field);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			Assert.IsTrue(cell.Body.IndexOf("null") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckNullWrong()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new NullKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("null");
			StringFixture fixture = new StringFixture();
			fixture.Field = "some value";
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual("some value", fixture.Field);
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
			Assert.IsTrue(cell.Body.IndexOf("null") > -1);
			Assert.IsTrue(cell.Body.IndexOf("some value") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}
	}
}