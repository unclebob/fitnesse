// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class ActionFixtureTest
	{
		private Parse table;
		private Fixture fixture;

		[SetUp]
		public void SetUp()
		{
			TestUtils.InitAssembliesAndNamespaces();
			fixture = new Fixture();
		}

		private string BuildTable(string name)
		{
			StringBuilder builder = new StringBuilder();
			builder.Append("<table border=\"1\" cellspacing=\"0\">");
			builder.Append("<tr><td colspan=\"3\">").Append(name).Append("</td></tr>");
			builder.Append("<tr><td>start</td><td colspan=\"2\">Count Fixture</td></tr>");
			builder.Append("<tr><td>check</td><td>Counter</td><td>0</td></tr>");
	
			builder.Append("<tr><td>press</td><td colspan=\"2\">Count</td></tr>");
			builder.Append("<tr><td>check</td><td>Counter</td><td>1</td></tr>");
			builder.Append("<tr><td>press</td><td colspan=\"2\">Count</td></tr>");
			builder.Append("<tr><td>check</td><td>Counter</td><td>2</td></tr>");
			builder.Append("<tr><td>enter</td><td>Counter</td><td>5</td></tr>");
	
			builder.Append("<tr><td>press</td><td colspan=\"2\">Count</td></tr>");
			builder.Append("<tr><td>check</td><td>Counter</td><td>6</td></tr>");
			builder.Append("</table>");
			return builder.ToString();
		}

		[Test]
		public void TestStart()
		{
			table = new Parse(BuildTable("ActionFixture"));
			fixture.DoTables(table);
			Assert.IsNotNull((CountFixture) Fixture.LastFixtureLoaded);
		}

		[Test]
		public void TestCheck()
		{
			table = new Parse(BuildTable("ActionFixture"));
			fixture.DoTables(table);
			Fixture countFixture = Fixture.LastFixtureLoaded;
			int actualCount = ((CountFixture)countFixture).Counter;
			Assert.AreEqual(6, actualCount);
			Assert.AreEqual(4, fixture.Counts.Right);
		}

		[Test]
		public void TestCheckOnTimedActionFixture()
		{
			table = new Parse(BuildTable("TimedActionFixture"));
			fixture.DoTables(table);
			Fixture countFixture = Fixture.LastFixtureLoaded;
			int actualCount = ((CountFixture)countFixture).Counter;
			Assert.AreEqual(6, actualCount);
			Assert.AreEqual(4, countFixture.Counts.Right);
		}
	}

	public class CountFixture : Fixture
	{
		private int counter = 0;

		public void Count()
		{
			counter++;
		}

		public int Counter
		{
			set { counter = value; }
			get { return counter; }
		}
	}
}