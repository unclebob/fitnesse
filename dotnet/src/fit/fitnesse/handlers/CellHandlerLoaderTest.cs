// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text;
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class CellHandlerLoaderTest
	{
		[SetUp]
		public void SetUp()
		{
			CellOperation.RemoveHandler(new SubstringHandler());
		}

		[Test]
		public void TestLoadHandler()
		{
			TestUtils.InitAssembliesAndNamespaces();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"2\">cell handler loader</td></tr>");
			builder.Append("<tr><td>load</td><td>substring handler</td></tr>");
			builder.Append("</table>");
			Assert.IsFalse(CellOperation.GetHandler("..sub..", null) is SubstringHandler);
			Fixture fixture = new Fixture();
			fixture.DoTables(new Parse(builder.ToString()));
			Assert.IsTrue(CellOperation.GetHandler("..sub..", null) is SubstringHandler);
		}

		[Test]
		public void TestRemoveHandler()
		{
			TestUtils.InitAssembliesAndNamespaces();
			CellOperation.LoadHandler(new SubstringHandler());
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"2\">CellHandlerLoader</td></tr>");
			builder.Append("<tr><td>remove</td><td>SubstringHandler</td></tr>");
			builder.Append("</table>");
			Assert.IsTrue(CellOperation.GetHandler("..sub..", null) is SubstringHandler);
			Fixture fixture = new Fixture();
			fixture.DoTables(new Parse(builder.ToString()));
			Assert.IsFalse(CellOperation.GetHandler("..sub..", null) is SubstringHandler);
		}
	}
}