// Fit parser for FitNesse .NET.
// Copyright (c) 2005 Syterra Software Inc. Released under the terms of the GNU General Public License version 2 or later.
// Based on designs from Fit (c) 2002 Cunningham & Cunningham, Inc., FitNesse by Object Mentor Inc., FitLibrary (c) 2003 Rick Mugridge, University of Auckland, New Zealand.

using System;
using System.Collections;

namespace fit {

    // Parses a HTML string, producing a Fit Parse tree.
    // Uses a recursive descent parsing approach.
    // The lexical analyzer is unusual - it skips everything until it finds the next expected token.
    public class HtmlParser {

        public static HtmlParser Instance {get {return myInstance;}}
        private static HtmlParser myInstance = new HtmlParser();

        public Parse Parse(string theInput) {
            AlternationParser alternationParser = new AlternationParser();
            ListParser cells = new ListParser("td", alternationParser, false);
            ListParser rows = new ListParser("tr", cells, true);
            ListParser tables = new ListParser("table", rows, true);
            ListParser items = new ListParser("li", alternationParser, false);
            ListParser lists = new ListParser("ul", items, true);
            alternationParser.ChildParsers = new ListParser[] {tables, lists};
            return tables.Parse(new LexicalAnalyzer(theInput));
        }

        private interface ElementParser {
            Parse Parse(LexicalAnalyzer theAnalyzer);
            string Keyword {get;}
        }

        private class ListParser: ElementParser {
            
            public ListParser(string theKeyword, ElementParser theChildParser, bool thisRequiresChildren) {
                myChildParser = theChildParser;
                myKeyword = theKeyword;
                IRequireChildren = thisRequiresChildren;
            }

            public string Keyword {get {return myKeyword;}}

            public Parse Parse(LexicalAnalyzer theAnalyzer) {
                Parse list = ParseOne(theAnalyzer);
                if (list != null) {
                    list.More = Parse(theAnalyzer);
                    if (list.More == null) list.Trailer = theAnalyzer.Trailer;
                }
                return list;
            }

            public Parse ParseOne(LexicalAnalyzer theAnalyzer) {
                theAnalyzer.GoToNextToken(myKeyword);
                if (theAnalyzer.Token.Length == 0) return null;
                return ParseElement(theAnalyzer);
            }

            private Parse ParseElement(LexicalAnalyzer theAnalyzer) {
                string tag = theAnalyzer.Token;
                string leader = theAnalyzer.Leader;
                theAnalyzer.PushEnd("/" + myKeyword);
                Parse children = myChildParser.Parse(theAnalyzer);
                if (IRequireChildren && children == null) {
                    throw new ApplicationException(string.Format("Can't find tag: {0}", myChildParser.Keyword));
                }
                theAnalyzer.PopEnd();
                theAnalyzer.GoToNextToken("/" + myKeyword);
                if (theAnalyzer.Token.Length == 0) throw new ApplicationException("expected </" + myKeyword + ">");
                return new Parse(tag, theAnalyzer.Token, leader, (children == null ? theAnalyzer.Leader : string.Empty), children);
            }

            private ElementParser myChildParser;
            private string myKeyword;
            private bool IRequireChildren;
        }

        private class AlternationParser: ElementParser {

            public Parse Parse(LexicalAnalyzer theAnalyzer) {
                Parse result = null;
                ListParser firstChildParser = null;
                int firstPosition = int.MaxValue;
                foreach (ListParser childParser in myChildParsers) {
                    int contentPosition = theAnalyzer.FindPosition(childParser.Keyword);
                    if (contentPosition >= 0 && contentPosition < firstPosition) {
                        firstPosition = contentPosition;
                        firstChildParser = childParser;
                    }
                }
                if (firstChildParser != null) {
                    result = firstChildParser.ParseOne(theAnalyzer);
                    result.More = Parse(theAnalyzer);
                }
                return result;
            }

            public string Keyword {get {return string.Empty;}}

            public ListParser[] ChildParsers {set {myChildParsers = value;}}

            private ListParser[] myChildParsers;
        }

        private class LexicalAnalyzer {

            public LexicalAnalyzer(string theInput) {
                myInput = theInput;
                myEndTokens = new Stack();
            }

            public void GoToNextToken(string theToken) {
                myToken = string.Empty;
                int start = myInput.ToLower().IndexOf("<" + theToken);
                if (start < 0 || start > EndPosition) return;
                myLeader = myInput.Substring(0, start);
                int end = myInput.IndexOf('>', start);
                if (end < 0) return;
                myToken = myInput.Substring(start, end - start + 1);
                myInput = myInput.Substring(end + 1);
            }

            public int FindPosition(string theToken) {
                int start = myInput.ToLower().IndexOf("<" + theToken);
                if (start < 0 || start > EndPosition) return -1;
                int end = myInput.IndexOf('>', start);
                if (end < 0) return -1;
                return start;
            }

            public string Trailer {
                get {
                    int endPosition = EndPosition;
                    string result = myInput.Substring(0, endPosition);
                    myInput = myInput.Substring(endPosition);
                    return result;
                }
            }

            public string PeekEnd() {
                string endToken = null;
                try {
                    endToken = (string)myEndTokens.Peek();
                }
                catch (InvalidOperationException) {}
                return endToken;
            }

            public void PushEnd(string theToken) {
                myEndTokens.Push(theToken);
            }

            public void PopEnd() {
                myEndTokens.Pop();
            }

            public string Leader {get {return myLeader;}}
            public string Token {get {return myToken;}}

            private int EndPosition {
                get {
                    int endInput = -1;
                    string endToken = PeekEnd();
                    if (endToken != null) {
                        endInput = myInput.ToLower().IndexOf("<" + endToken);
                    }
                    if (endInput < 0) endInput = myInput.Length;
                    return endInput;
                }
            }

            private string myInput;
            private string myLeader;
            private string myToken;
            private Stack myEndTokens;
        }
    }
}
