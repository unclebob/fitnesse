// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class GracefulNameTest
	{
		private GracefulNameConverter converter;
		private string uglyString;

		[SetUp]
		public void SetUp() 
		{
			converter = new GracefulNameConverter();
			uglyString = "?&_)*( bad&^%$*()(*&)compAny~`+=-_,";
		}

		[Test]
		public void TestGracefulNameConverterOnTypeName() 
		{
			Assert.AreEqual("badcompany", converter.GetConvertedName(uglyString));
			Assert.AreEqual("badcompany", converter.GetConvertedName("Bad.Company"));
			Assert.AreEqual("badcompany", converter.GetConvertedName("BadCompany"));
			Assert.AreEqual("badcompany", converter.GetConvertedName("Bad Company"));
			Assert.AreEqual("badcompany", converter.GetConvertedName("bad company"));
			Assert.AreEqual("badcompany", converter.GetConvertedName("Bad-Company"));
			Assert.AreEqual("badcompany", converter.GetConvertedName("Bad Company."));
			Assert.AreEqual("badcompany", converter.GetConvertedName("(Bad Company)"));
			Assert.AreEqual("bad123company", converter.GetConvertedName("bad 123 company"));
			Assert.AreEqual("bad123company", converter.GetConvertedName("bad 123company"));
			Assert.AreEqual("bad123company", converter.GetConvertedName("   bad  \t123  company   "));
		}

		[Test]
		public void TestConvertMemberName()
		{
			Assert.AreEqual("somemembername", converter.GetConvertedName("Some Member Name"));
			Assert.AreEqual("somemembername", converter.GetConvertedName("Some Member Name?"));
			Assert.AreEqual("somemembername", converter.GetConvertedName("Some Member Name!"));
			Assert.AreEqual("somemembername", converter.GetConvertedName("Some Member Name()"));
			Assert.AreEqual("member1name", converter.GetConvertedName("Member 1 Name."));
		}

		[Test]
		public void TestIsNumber() 
		{
			string isNumber = "123";
			Assert.IsTrue(converter.IsNumber(isNumber));
			string isNotNumber = "12h34";
			Assert.IsFalse(converter.IsNumber(isNotNumber));
		}

		[Test]
		public void TestSeparateTwoCharacters() 
		{
			Assert.AreEqual("a b", converter.SeparateTwoCharacters("ab"));
		}

		[Test]
		public void TestSeparateWords() 
		{
			string input = "  one  two three \t \n four";
			Assert.AreEqual(4, converter.GetListOfSeparatedWords(input).Count);
		}

		[Test]
		public void TestMergeWords() {
			string[] words = {"one","two","three","four"};
			Assert.AreEqual("onetwothreefour", converter.MergeWords(words));
		}

		[Test]
		public void TestReplaceIllegalCharactersWithSpaces() 
		{
			Assert.AreEqual("       bad           compAny       ", converter.ReplaceIllegalCharactersWithSpaces(uglyString));
		}

		[Test]
		public void TestRemoveLastCharacter() 
		{
			Assert.AreEqual("abcd", converter.RemoveLastCharacter("abcde"));
		}

		[Test]
		public void TestRemoveTrailingPeriods() 
		{
			Assert.AreEqual("abcd", converter.RemoveTrailingPeriods("abcd."));
			Assert.AreEqual("abcd", converter.RemoveTrailingPeriods("abcd.."));
			Assert.AreEqual("a.b.c.d", converter.RemoveTrailingPeriods("a.b.c.d..."));
		}
	}
}