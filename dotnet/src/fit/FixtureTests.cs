// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class FixtureTest
	{
		[SetUp]
		public void SetUp()
		{
			TestUtils.InitAssembliesAndNamespaces();
		}

		[Test]
		public void testMissingFixtureError()
		{
			string message = null;
			Fixture fixture = new Fixture();
			try
			{
				fixture.LoadFixture("NonExistentFixture");
			}
			catch (Exception e)
			{
				message = e.Message;
			}
			Assert.IsTrue(message.IndexOf("Assemblies searched") > 0);
			Assert.IsTrue(message.ToLower().IndexOf("fit.dll") > 0, message);
		}

		[Test]
		public void TestFixtureParametersNoAdditionalCell()
		{
			string tableString = "<table><tr><td>StringFixture</td></tr><tr><td>field</td><td>field</td></tr></table>";
			Fixture fixture = new Fixture();
			fixture.DoTables(new Parse(tableString));
			Fixture stringFixture = Fixture.LastFixtureLoaded;
			Assert.AreEqual(0, stringFixture.Args.Length);
		}

		[Test]
		public void TestFixtureParametersOneParam()
		{
			string arg = "I'd like to buy an argument";
			string tableString = "<table><tr><td>StringFixture</td><td>" + arg + "</td></tr><tr><td>field</td><td>field</td></tr></table>";
			Fixture fixture = new Fixture();
			fixture.DoTables(new Parse(tableString));
			Fixture stringFixture = Fixture.LastFixtureLoaded;
			Assert.AreEqual(1, stringFixture.Args.Length);
			Assert.AreEqual(arg, stringFixture.Args[0]);
		}

		[Test]
		public void TestFixtureParametersTwoParams()
		{
			string arg1 = "I'd like to buy an argument";
			string arg2 = "I'd like to buy another argument";
			string tableString = "<table><tr><td>StringFixture</td><td>" + arg1 + "</td><td>" + arg2 + "</td></tr><tr><td>field</td><td>field</td></tr></table>";
			Fixture fixture = new Fixture();
			fixture.DoTables(new Parse(tableString));
			Fixture stringFixture = Fixture.LastFixtureLoaded;
			Assert.AreEqual(2, stringFixture.Args.Length);
			Assert.AreEqual(arg1, stringFixture.Args[0]);
			Assert.AreEqual(arg2, stringFixture.Args[1]);
		}
	}

	[TestFixture]
	public class EscapeTest
	{
		[Test]
		public void TestEscape()
		{
			String junk = "!@#$%^*()_-+={}|[]\\:\";',./?`";
			Assert.AreEqual(junk, Fixture.Escape(junk));
			Assert.AreEqual("", Fixture.Escape(""));
			Assert.AreEqual("&lt;", Fixture.Escape("<"));
			Assert.AreEqual("&lt;&lt;", Fixture.Escape("<<"));
			Assert.AreEqual("x&lt;", Fixture.Escape("x<"));
			Assert.AreEqual("&amp;", Fixture.Escape("&"));
			Assert.AreEqual("&lt;&amp;&lt;", Fixture.Escape("<&<"));
			Assert.AreEqual("&amp;&lt;&amp;", Fixture.Escape("&<&"));
			Assert.AreEqual("a &lt; b &amp;&amp; c &lt; d", Fixture.Escape("a < b && c < d"));
		}
	}

	[TestFixture]
	public class SaveAndRecallTest
	{
		[TearDown]
		public void TearDown()
		{
			Fixture.ClearSaved();
		}

		[Test]
		public void TestSaveAndRecallValue()
		{
			string key = "aVariable";
			object value = "aValue";
			Assert.IsNull(Fixture.Recall(key));
			Fixture.Save(key, value);
			Assert.AreEqual(value, Fixture.Recall(key));
		}

		[Test]
		public void TestSaveAndRecallTwoValues()
		{
			string key = "aVariable";
			object value = "aValue";
			string otherKey = "anotherVariable";
			object otherValue = "anotherValue";
			Assert.IsNull(Fixture.Recall(key));
			Fixture.Save(key, value);
			Fixture.Save(otherKey, otherValue);
			Assert.AreEqual(value, Fixture.Recall(key));
			Assert.AreEqual(otherValue, Fixture.Recall(otherKey));
		}

		[Test]
		public void TestSaveAndRecallChangedValue()
		{
			string key = "aVariable";
			object value = "aValue";
			object otherValue = "anotherValue";
			Fixture.Save(key, value);
			Fixture.Save(key, otherValue);
			Assert.AreEqual(otherValue, Fixture.Recall(key));
		}

		[Test]
		public void TestGetTargetType()
		{
			Fixture fixture = new Fixture();
			Assert.AreSame(fixture, fixture.GetTargetObject());
		}
	}
}