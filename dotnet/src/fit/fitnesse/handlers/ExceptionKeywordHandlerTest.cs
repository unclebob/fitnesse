// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class ExceptionKeywordHandlerTest
	{
		[Test]
		public void TestRegisterAndGetsErrorKeywordOperator()
		{
			CellOperation.LoadHandler(new ExceptionKeywordHandler());
			Assert.IsTrue(CellOperation.GetHandler("exception[]", null) is ExceptionKeywordHandler);
			Assert.IsTrue(CellOperation.GetHandler("exception[NullPointerException]", null) is ExceptionKeywordHandler);
		}

		[TearDown]
		public void TearDown()
		{
			CellOperation.ClearHandlers();
			CellOperation.LoadDefaultHandlers();
		}

		[Test]
		public void TestDoCheckExceptionClassRight()
		{
			ObjectFactory.AddNamespace("System");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new ExceptionKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("exception[NullReferenceException]");
			ExceptionThrowingFixture fixture = new ExceptionThrowingFixture();
			CellOperation.Check(fixture, "ThrowNullReferenceException", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckErrorClassWrong()
		{
			ObjectFactory.AddNamespace("System");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new ExceptionKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("exception[NullReferenceException]");
			ExceptionThrowingFixture fixture = new ExceptionThrowingFixture();
			CellOperation.Check(fixture, "ThrowApplicationException", cell);
			Assert.IsTrue(cell.Tag.IndexOf("fail") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestDoCheckExceptionMessageRight()
		{
			ObjectFactory.AddNamespace("System");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new ExceptionKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("exception[\"an exception\"]");
			ExceptionThrowingFixture fixture = new ExceptionThrowingFixture();
			fixture.Message = "an exception";
			CellOperation.Check(fixture, "ThrowApplicationException", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestDoCheckExceptionMessageAndClassRight()
		{
			ObjectFactory.AddNamespace("System");
			CellOperation.LoadDefaultHandler(new DefaultCellHandler());
			CellOperation.LoadHandler(new ExceptionKeywordHandler());
			Parse cell = CellHandlerTestUtils.CreateCell("exception[ApplicationException: \"an exception\"]");
			ExceptionThrowingFixture fixture = new ExceptionThrowingFixture();
			fixture.Message = "an exception";
			CellOperation.Check(fixture, "ThrowApplicationException", cell);
			Assert.IsTrue(cell.Tag.IndexOf("pass") > -1);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}
	}
}