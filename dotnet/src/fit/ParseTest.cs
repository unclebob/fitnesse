// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class ParseTest
	{

		[Test]
		public void TestUnEscape() 
		{
			Assert.AreEqual("a<b", Parse.UnEscape("a&lt;b"));
			Assert.AreEqual("a>b & b>c &&", Parse.UnEscape("a&gt;b&nbsp;&amp;&nbsp;b>c &&"));
			Assert.AreEqual("&amp;&amp;", Parse.UnEscape("&amp;amp;&amp;amp;"));
			Assert.AreEqual("a>b & b>c &&", Parse.UnEscape("a&gt;b&nbsp;&amp;&nbsp;b>c &&"));
		}

		[Test]
		public void TestUnFormat() 
		{
			Assert.AreEqual("ab",Parse.UnFormat("<font size=+1>a</font>b"));
			Assert.AreEqual("ab",Parse.UnFormat("a<font size=+1>b</font>"));
			Assert.AreEqual("a<b",Parse.UnFormat("a<b"));
		}

		[Test]
		public void TestParsing()
		{
			Parse p = new Parse("leader<Table foo=2>body</table>trailer", new string[] {"table"});
			Assert.AreEqual("leader", p.Leader);
			Assert.AreEqual("<Table foo=2>", p.Tag);
			Assert.AreEqual("body", p.Body);
			Assert.AreEqual("trailer", p.Trailer);
		}

		[Test]
		public void TestRecursing() 
		{
			Parse p = new Parse("leader<table><TR><Td>body</tD></TR></table>trailer");
			Assert.AreEqual(null, p.Body);
			Assert.AreEqual(null, p.Parts.Body);
			Assert.AreEqual("body", p.Parts.Parts.Body);
		}

		[Test]
		public void TestIterating()
		{
			Parse p = new Parse("leader<table><tr><td>one</td><td>two</td><td>three</td></tr></table>trailer");
			Assert.AreEqual("one", p.Parts.Parts.Body);
			Assert.AreEqual("two", p.Parts.Parts.More.Body);
			Assert.AreEqual("three", p.Parts.Parts.More.More.Body);
		}

		[Test]
		public void TestIndexing() 
		{
			Parse p = new Parse("leader<table><tr><td>one</td><td>two</td><td>three</td></tr><tr><td>four</td></tr></table>trailer");
			Assert.AreEqual("one", p.At(0,0,0).Body);
			Assert.AreEqual("two", p.At(0,0,1).Body);
			Assert.AreEqual("three", p.At(0,0,2).Body);
			Assert.AreEqual("three", p.At(0,0,3).Body);
			Assert.AreEqual("three", p.At(0,0,4).Body);
			Assert.AreEqual("four", p.At(0,1,0).Body);
			Assert.AreEqual("four", p.At(0,1,1).Body);
			Assert.AreEqual("four", p.At(0,2,0).Body);
			Assert.AreEqual(1, p.Size);
			Assert.AreEqual(2, p.Parts.Size);
			Assert.AreEqual(3, p.Parts.Parts.Size);
			Assert.AreEqual("one", p.Leaf.Body);
			Assert.AreEqual("four", p.Parts.Last.Leaf.Body);
		}

		[Test]
		public void TestParseException() 
		{
			try 
			{
				new Parse("leader<table><tr><th>one</th><th>two</th><th>three</th></tr><tr><td>four</td></tr></table>trailer");
				Assert.Fail("expected Exception not thrown");
			} 
			catch (ApplicationException e) 
			{
				Assert.AreEqual("Can't find tag: td", e.Message);
				return;
			}
		}

		[Test]
		public void TestText() 
		{
			string[] tags ={"td"};
			Parse p = new Parse("<td>a&lt;b</td>", tags);
			Assert.AreEqual("a&lt;b", p.Body);
			Assert.AreEqual("a<b", p.Text);
			p = new Parse("<td>\ta&gt;b&nbsp;&amp;&nbsp;b>c &&&nbsp;</td>", tags);
			Assert.AreEqual("\ta>b & b>c && ", p.Text);
			p = new Parse("<td>\ta&gt;b&nbsp;&amp;&nbsp;b>c &&nbsp;</td>", tags);
			Assert.AreEqual("\ta>b & b>c & ", p.Text);
			p = new Parse("<TD><P><FONT FACE=\"Arial\" SIZE=2>GroupTestFixture</FONT></TD>", tags);
			Assert.AreEqual("GroupTestFixture",p.Text);
		}
	}
}
