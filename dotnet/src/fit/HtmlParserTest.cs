// Fit parser for FitNesse .NET.
// Copyright (c) 2005 Syterra Software Inc. Released under the terms of the GNU General Public License version 2 or later.
// Based on designs from Fit (c) 2002 Cunningham & Cunningham, Inc., FitNesse by Object Mentor Inc., FitLibrary (c) 2003 Rick Mugridge, University of Auckland, New Zealand.

using System;
using System.Text;
using NUnit.Framework;

namespace fit {

	[TestFixture] public class HtmlParserTest {

        [Test] public void ParseEmpty() {
            Parse result = HtmlParser.Instance.Parse(string.Empty);
            Assert.IsTrue(result == null);
        }
    
        [Test] public void ParseNoTables() {
            Parse result = HtmlParser.Instance.Parse("set the table");
            Assert.IsTrue(result == null);
            result = (new HtmlParser()).Parse("set the <table");
            Assert.IsTrue(result == null);
        }
    
        [Test, ExpectedException(typeof(ApplicationException))] public void ParseEmptyTable() {
            HtmlParser.Instance.Parse("leader<table x=\"y\"></table>trailer");
        }
    
        [Test, ExpectedException(typeof(ApplicationException))] public void ParseTableWithBody() {
            HtmlParser.Instance.Parse("leader<Table foo=2>body</table>trailer");
        }
    
        [Test] public void ParseTwoTables() {
            string result = Format(HtmlParser.Instance.Parse("<table><tr><td>x</td></tr></table>leader<table><tr><td>x</td></tr></table>trailer"), " ");
            Assert.AreEqual("<table> <tr> <td> x</td></tr></table> leader <table> <tr> <td> x</td></tr></table> trailer", result);
        }

        [Test, ExpectedException(typeof(ApplicationException))] public void ParseRow() {
            HtmlParser.Instance.Parse("<table>leader<tr></tr></table>");
        }
    
        [Test] public void ParseCell() {
            string result = Format(HtmlParser.Instance.Parse("<table x=\"y\"><tr><td>content</td></tr></table>"), " ");
            Assert.AreEqual("<table x=\"y\"> <tr> <td> content</td></tr></table>", result);
        }
    
        [Test] public void ParseCellMixedCase() {
            string result = Format(HtmlParser.Instance.Parse("leader<table><TR><Td>body</tD></TR></table>trailer"), " ");
            Assert.AreEqual("leader <table> <TR> <Td> body</tD></TR></table> trailer", result);
        }
    
        [Test] public void ParseNestedTable() {
            string result = Format(HtmlParser.Instance.Parse("<table><tr><td><table><tr><td>content</td></tr></table></td></tr></table>"), " ");
            Assert.AreEqual("<table> <tr> <td> <table> <tr> <td> content</td></tr></table></td></tr></table>", result);
        }
    
        [Test] public void ParseNestedList() {
            string result = Format(HtmlParser.Instance.Parse("<table><tr><td><ul><li>content</li></ul></td></tr></table>"), " ");
            Assert.AreEqual("<table> <tr> <td> <ul> <li> content</li></ul></td></tr></table>", result);
        }
    
        [Test] public void ParseNestedListAndTable() {
            string result = Format(HtmlParser.Instance.Parse("<table><tr><td><ul><li>content</li></ul><table><tr><td>content</td></tr></table></td></tr></table>"), " ");
            Assert.AreEqual("<table> <tr> <td> <ul> <li> content</li></ul> <table> <tr> <td> content</td></tr></table></td></tr></table>", result);
        }

        private string Format(Parse theParseTree, string theSeparator) {
            StringBuilder result = new StringBuilder();
            if (theParseTree.Leader != null && theParseTree.Leader.Length > 0) result.AppendFormat("{0}{1}", theParseTree.Leader, theSeparator);
            result.Append(theParseTree.Tag);
            if (theParseTree.Body != null && theParseTree.Body.Length > 0) result.AppendFormat("{0}{1}", theSeparator, theParseTree.Body);
            if (theParseTree.Parts != null) result.AppendFormat("{0}{1}", theSeparator, Format(theParseTree.Parts, theSeparator));
            result.Append(theParseTree.End);
            if (theParseTree.Trailer != null && theParseTree.Trailer.Length > 0) result.AppendFormat("{0}{1}", theSeparator, theParseTree.Trailer);
            if (theParseTree.More != null) result.AppendFormat("{0}{1}", theSeparator, Format(theParseTree.More, theSeparator));
            return result.ToString();
        }
    }
}
