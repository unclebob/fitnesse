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
		public void UnEscapeShouldRemoveHtmlEscapes() 
		{
			Assert.AreEqual("a<b", Parse.UnEscape("a&lt;b"));
			Assert.AreEqual("a>b & b>c &&", Parse.UnEscape("a&gt;b&nbsp;&amp;&nbsp;b>c &&"));
			Assert.AreEqual("&amp;&amp;", Parse.UnEscape("&amp;amp;&amp;amp;"));
			Assert.AreEqual("a>b & b>c &&", Parse.UnEscape("a&gt;b&nbsp;&amp;&nbsp;b>c &&"));
		}

		[Test]
		public void UnFormatShouldRemoveHtmlFormattingCodeIfPresent() 
		{
			Assert.AreEqual("ab",Parse.UnFormat("<font size=+1>a</font>b"));
			Assert.AreEqual("ab",Parse.UnFormat("a<font size=+1>b</font>"));
			Assert.AreEqual("a<b",Parse.UnFormat("a<b"));
		}

		[Test]
		public void LeaderShouldReturnAllHtmlTextBeforeTheParse()
		{
			Parse p = new Parse("<html><head></head><body><Table foo=2>body</table></body></html>", new string[] {"table"});
			Assert.AreEqual("<html><head></head><body>", p.Leader);
			Assert.AreEqual("<Table foo=2>", p.Tag);
			Assert.AreEqual("body", p.Body);
			Assert.AreEqual("</body></html>", p.Trailer);
		}

		private Parse SimpleTableParse
		{
			get { return new Parse("leader<table><tr><td>body</td></tr></table>trailer"); }
		}

		[Test]
		public void BodyShouldReturnNullForTables() 
		{
			Parse parse = SimpleTableParse;
			Assert.AreEqual(null, parse.Body);
		}

		[Test]
		public void BodyShouldReturnNullForRows() 
		{
			Parse parse = SimpleTableParse;
			Assert.AreEqual(null, parse.Parts.Body);
		}

		[Test]
		public void BodyShouldReturnTextForCells() 
		{
			Parse parse = SimpleTableParse;
			Assert.AreEqual("body", parse.Parts.Parts.Body);
		}

		[Test]
		public void PartsShouldReturnCellsWhenTheParseRepresentsARow()
		{
			Parse row = new Parse("<tr><td>one</td><td>two</td><td>three</td></tr>", new string[]{"tr", "td"});
			Assert.AreEqual("one", row.Parts.Body);
			Assert.AreEqual("two", row.Parts.More.Body);
			Assert.AreEqual("three", row.Parts.More.More.Body);
		}

		[Test]
		public void PartsShouldReturnRowsWhenTheParseRepresentsATable()
		{
			Parse table = new Parse("<table><tr><td>row one</td></tr><tr><td>row two</td></tr></table>", new string[] {"table", "tr", "td"});
			Assert.AreEqual("row one", table.Parts.Parts.Body);
			Assert.AreEqual("row two", table.Parts.More.Parts.Body);
		}

		[Test]
		public void TestIndexingPage() 
		{
			Parse p = new Parse(
				@"leader
					<table>
						<tr>
							<td>one</td><td>two</td><td>three</td>
						</tr>
						<tr>
							<td>four</td>
						</tr>
					</table>
				trailer");
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
