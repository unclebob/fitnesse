// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using NUnit.Framework;

namespace fitnesse.handlers
{
	[TestFixture]
	public class SymbolHandlerTest
	{
		[SetUp]
		public void SetUp()
		{
			CellOperation.ClearHandlers();
			CellOperation.LoadDefaultHandlers();
		}

		[Test]
		public void TestRegisterAndGet()
		{
			Assert.IsTrue(CellOperation.GetHandler("<<xyz", null) is SymbolRecallHandler);
			Assert.IsFalse(CellOperation.GetHandler("x<<yz", null) is SymbolSaveHandler);
			Assert.IsFalse(CellOperation.GetHandler("x<<yz", null) is SymbolRecallHandler);
			Assert.IsTrue(CellOperation.GetHandler(">>xyz", null) is SymbolSaveHandler);
			Assert.IsFalse(CellOperation.GetHandler("x>>yz", null) is SymbolSaveHandler);
			Assert.IsFalse(CellOperation.GetHandler("x>>yz", null) is SymbolRecallHandler);
		}

		[Test]
		public void TestSaveString() {
			Parse cell = CellHandlerTestUtils.CreateCell(">>xyz");
			StringFixture fixture = new StringFixture();
			fixture.Field = "abc";
			CellOperation.Check(fixture, "Field", cell);
			Assert.AreEqual("abc", Fixture.Recall("xyz"));
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestRecallString() {
			Parse cell = CellHandlerTestUtils.CreateCell("<<def");
			StringFixture fixture = new StringFixture();
			Fixture.Save("def","ghi");
			CellOperation.Input(fixture, "Field", cell);
			Assert.AreEqual("ghi", fixture.Field);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestEvaluateRecallStringPass() {
			Parse cell = CellHandlerTestUtils.CreateCell("<<def");
			StringFixture fixture = new StringFixture();
			Fixture.Save("def","ghi");
			fixture.Field = "ghi";
			Assert.IsTrue(CellOperation.Evaluate(fixture, "Field", cell));
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestEvaluateRecallStringFail() {
			Parse cell = CellHandlerTestUtils.CreateCell("<<def");
			StringFixture fixture = new StringFixture();
			Fixture.Save("def","ghi");
			fixture.Field = "not ghi";
			Assert.IsFalse(CellOperation.Evaluate(fixture, "Field", cell));
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestEvaluateRecallStringFailNull() {
			Parse cell = CellHandlerTestUtils.CreateCell("<<def");
			StringFixture fixture = new StringFixture();
			Fixture.Save("def","ghi");
			fixture.Field = null;
			Assert.IsFalse(CellOperation.Evaluate(fixture, "Field", cell));
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 0, 0, 0);
		}

		[Test]
		public void TestCheckRecallValuePass()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("<<theKey");
			StringFixture fixture = new StringFixture();
			Fixture.Save("theKey","theValue");
			fixture.Field = "theValue";
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestCheckRecallValuePassPerson()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("<<thePerson");
			PersonFixture fixture = new PersonFixture();
			Person person = new Person("Eeek", "Gadd");
			Fixture.Save("thePerson", person);
			fixture.Field = person;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 1, 0, 0, 0);
		}

		[Test]
		public void TestCheckRecallValueFail()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("<<theKey");
			StringFixture fixture = new StringFixture();
			Fixture.Save("theKey","theValue");
			fixture.Field = "anotherValue";
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}

		[Test]
		public void TestCheckRecallValueFailPerson()
		{
			Parse cell = CellHandlerTestUtils.CreateCell("<<thePerson");
			PersonFixture fixture = new PersonFixture();
			Person person = new Person("Eeek", "Gadd");
			Person person2 = new Person("Eeek", "Gadds");
			Fixture.Save("thePerson", person);
			fixture.Field = person2;
			CellOperation.Check(fixture, "Field", cell);
			CellHandlerTestUtils.VerifyCounts(fixture, 0, 1, 0, 0);
		}
	}
}