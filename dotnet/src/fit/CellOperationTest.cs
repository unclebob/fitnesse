// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
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
