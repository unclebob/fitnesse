// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class DefaultCellHandlerTest
	{

		[SetUp]
		public void SetUp()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
		}

		[Test]
		public void TestRegisterDefaultOperator()
		{
			Assert.IsTrue(CellOperation.DefaultHandler is DefaultCellHandler);
		}

		[Test]
		public void TestDoInput()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("xyz");
			StringFixture fixture = new StringFixture();
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual("xyz", fixture.Field);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckCellRight()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("xyz");
			StringFixture fixture = new StringFixture();
			fixture.Field = "xyz";
			CellOperation.Check(fixture, "Field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckCellWrong() {
			Parse cell = CellHandlerTestUtils.CreateCell("xyz");
			StringFixture fixture = new StringFixture();
			fixture.Field = "abc";
			CellOperation.Check(fixture, "Field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
			Assert.IsTrue(cell.Body.IndexOf("abc") > -1);
			Assert.IsTrue(cell.Body.IndexOf("xyz") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestDoCheckCellWrongNull() {
			Parse cell = CellHandlerTestUtils.CreateCell("xyz");
			StringFixture fixture = new StringFixture();
			fixture.Field = null;
			CellOperation.Check(fixture, "Field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
			Assert.IsTrue(cell.Body.IndexOf("null") > -1);
			Assert.IsTrue(cell.Body.IndexOf("xyz") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}


		[Test]
		public void TestExecute()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("do");
			FixtureWithExecutableMethod fixture = new FixtureWithExecutableMethod();
			CellOperation.Execute(fixture, "Do", cell);
			Assert.AreEqual(1, FixtureWithExecutableMethod.Calls);
		}

		[Test]
		public void TestEvaluateWrong() {
			Parse cell = CellHandlerTestUtils.CreateCell("xyz");
			StringFixture fixture = new StringFixture();
			fixture.Field = "abc";
			Assert.IsFalse(CellOperation.Evaluate(fixture, "Field", cell));
			Assert.IsFalse(cell.Tag.IndexOf("fail") > -1);
			Assert.IsFalse(cell.Body.IndexOf("abc") > -1);
			Assert.IsTrue(cell.Body.IndexOf("xyz") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
			
		}

		[Test]
		public void TestEvaluateRight() {
			Parse cell = CellHandlerTestUtils.CreateCell("xyz");
			StringFixture fixture = new StringFixture();
			fixture.Field = "xyz";
			Assert.IsTrue(CellOperation.Evaluate(fixture, "Field", cell));
			Assert.IsFalse(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		class FixtureWithExecutableMethod : Fixture
		{
			public static int Calls = 0;

			public void Do()
			{
				Calls++;
			}
		}

	}
}