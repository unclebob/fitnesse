// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Collections;
using System.Text;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class ColumnFixtureTests
	{
		[Test]
		public void TestNullCell()
		{
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"2\">string fixture</td></tr>");
			builder.Append("<tr><td>Field</td><td>Field?</td></tr>");
			builder.Append("<tr><td></td><td>null</td></tr>");
			builder.Append("</table>");

			Parse parse = new Parse(builder.ToString());

			TestUtils.InitAssembliesAndNamespaces();
			Fixture fixture = new Fixture();
			fixture.DoTables(parse);
			Assert.AreEqual(1, fixture.Counts.Right);
			Assert.AreEqual(0, fixture.Counts.Wrong);
			Assert.AreEqual(0, fixture.Counts.Ignores);
			Assert.AreEqual(0, fixture.Counts.Exceptions);
		}

		[Test]
		public void TestBlankCell()
		{
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"6\">string fixture</td></tr>");
			builder.Append("<tr><td>field</td><td>field?</td><td>property</td><td>property?</td><td>set</td><td>get?</td></tr>");
			builder.Append("<tr><td>blank</td><td>blank</td><td>blank</td><td>blank</td><td>blank</td><td>blank</td></tr>");
			builder.Append("</table>");

			Parse parse = new Parse(builder.ToString());

			TestUtils.InitAssembliesAndNamespaces();
			Fixture fixture = new Fixture();
			fixture.DoTables(parse);
			Assert.AreEqual(3, fixture.Counts.Right);
			Assert.AreEqual(0, fixture.Counts.Wrong);
			Assert.AreEqual(0, fixture.Counts.Ignores);
			Assert.AreEqual(0, fixture.Counts.Exceptions);
		}

		[Test]
		public void TestExecuteAtEnd()
		{
			TestUtils.InitAssembliesAndNamespaces();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"2\">ExecuteTestFixture</td></tr>");
			builder.Append("<tr><td>Property</td><td>Property</td></tr>");
			builder.Append("<tr><td>first call</td><td>second call</td></tr>");
			builder.Append("</table>");
			Parse table = new Parse(builder.ToString());
			Fixture fixture = new Fixture();
			fixture.DoTables(table);
			ExecuteTestFixture testFixture = (ExecuteTestFixture) Fixture.LastFixtureLoaded;
			Assert.AreEqual("first call", testFixture.Values[0]);
			Assert.AreEqual("second call", testFixture.Values[1]);
			Assert.AreEqual("Execute()", testFixture.Values[2]);
		}

		[Test]
		public void TestExecuteInMiddle()
		{
			TestUtils.InitAssembliesAndNamespaces();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"2\">ExecuteTestFixture</td></tr>");
			builder.Append("<tr><td>Property</td><td>Property?</td><td>Property</td></tr>");
			builder.Append("<tr><td>first call</td><td>null</td><td>second call</td></tr>");
			builder.Append("</table>");
			Parse table = new Parse(builder.ToString());
			Fixture fixture = new Fixture();
			fixture.DoTables(table);
			ExecuteTestFixture testFixture = (ExecuteTestFixture) Fixture.LastFixtureLoaded;
			Assert.AreEqual("first call", testFixture.Values[0]);
			Assert.AreEqual("Execute()", testFixture.Values[1]);
			Assert.AreEqual("second call", testFixture.Values[2]);
		}

		[Test]
		public void TestGetTargetObject() {
			Fixture fixture = new ExecuteTestFixture();
			Assert.AreEqual(fixture, fixture.GetTargetObject());
		}

		[Test]
		public void TestEmptyHeaderCell()
		{
			TestUtils.InitAssembliesAndNamespaces();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan=\"2\">string fixture</td></tr>");
			builder.Append("<tr><td>field</td><td></td></tr>");
			builder.Append("<tr><td>some value</td><td>this is a comment</td></tr>");
			builder.Append("</table>");
			Parse table = new Parse(builder.ToString());
			Fixture fixture = new Fixture();
			fixture.DoTables(table);
			Assert.AreEqual(0, fixture.Counts.Right);
			Assert.AreEqual(0, fixture.Counts.Wrong);
			Assert.AreEqual(0, fixture.Counts.Ignores);
			Assert.AreEqual(0, fixture.Counts.Exceptions);
		}
	}

	public class ExecuteTestFixture : ColumnFixture
	{
		public IList Values = new ArrayList();

		public string Property
		{
			get { return null; }
			set { Values.Add(value); }
		}

		public override void Execute()
		{
			Values.Add("Execute()");
		}
	}
}