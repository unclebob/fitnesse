// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class ErrorKeywordHandlerTest
	{
		[Test]
		public void TestRegisterAndGetsErrorKeywordOperator()
		{
			CellOperation.LoadHandler(new ErrorKeywordHandler());
			Assert.IsTrue(CellOperation.GetHandler("error", null) is ErrorKeywordHandler);
		}

		[Test]
		public void TestDoCheckErrorRight()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new ErrorKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("error");
			ErrorThrowingFixture fixture = new ErrorThrowingFixture();
			CellOperation.Check(fixture, "ErrorThrowingMethod", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			Assert.IsTrue(cell.Body.IndexOf("error") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckErrorWrong()
		{
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new ErrorKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("error");
			StringFixture fixture = new StringFixture();
			fixture.Field = "some value";
			CellOperation.Check(fixture, "Field", cell);
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
			Assert.IsTrue(cell.Body.IndexOf("error") > -1);
			Assert.IsTrue(cell.Body.IndexOf("some value") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}
	}
}