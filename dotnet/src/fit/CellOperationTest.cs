// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fitnesse.handlers;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class CellOperationTest
	{
		[TearDown]
		public void TearDown()
		{
			CellOperation.RemoveHandler(new CustomNullKeywordHandler());
		}

		[Test]
		public void TestThatRegisterAddsHandlerToFrontOfList()
		{
			CellOperation.LoadDefaultHandlers();
			CellOperation.LoadHandler(new CustomNullKeywordHandler());
			Assert.IsTrue(CellOperation.GetHandler("null", null) is CustomNullKeywordHandler);
		}

		private class CustomNullKeywordHandler : AbstractCellHandler
		{
			public override bool Match(string searchString, System.Type type) {
				return "null".Equals(searchString);
			}

		}
	}
}
