// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using NUnit.Framework;

namespace fit
{

	[TestFixture]
	public class FixtureTest
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

		[Test]
		public void TestSaveAndRecallValue()
		{
			string key = "aVariable";
			object value = "aValue";
			Assert.IsNull(Fixture.Recall(key));
			Fixture.Save(key, value);
			Assert.AreEqual(value, Fixture.Recall(key));
		}
	}
}
