// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using System.Text;
using fitnesse.handlers;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class RowFixtureTest
	{
		public void TestExpectBlankOrNullAllCorrect()
		{
			TestUtils.InitAssembliesAndNamespaces();
			DoTable(
				BuildTable(new string[] {"null", "blank", "joe"}),
				BuildObjectArray(new string[]{null, "", "joe"}), 
				3, 0, 0, 0
				);
			DoTable(
				BuildTable(new string[] {"Null", "Blank"}),
				BuildObjectArray(new string[]{null, ""}), 
				2, 0, 0, 0
				);
			DoTable(
				BuildTable(new string[] {"NULL", "BLANK"}),
				BuildObjectArray(new string[]{null, ""}), 
				2, 0, 0, 0
				);
		}

		public void TestExpectBlankOrNullSomeWrong()
		{
			TestUtils.InitAssembliesAndNamespaces();
			Parse table = BuildTable(new string[] {"blank", "null"});
			DoTable(
				table,
				BuildObjectArray(new string[]{"", "this is not null"}), 
				1, 2, 0, 0
			);
		}

		private static Parse BuildTable(string[] values)
		{
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td>BusinessObjectRowFixture</td></tr>");
			builder.Append("<tr><td>GetFirstString</td></tr>");
			foreach (string value in values)
			{
				builder.Append("<tr><td>" + value + "</td></tr>");
			}
			builder.Append("</table>");
			return new Parse(builder.ToString());
		}

		private static object[] BuildObjectArray(string[] values)
		{
			object[] objects = new object[values.Length];
			int count = 0;
			foreach (string value in values)
			{
				objects[count++] = new BusinessObject(new string[] {value});
			}
			return objects;
		}

		public void DoTable(Parse tables, object[] businessObjects, int right, int wrong, int ignores, int exceptions)
		{
			Fixture fixture = new Fixture();
			BusinessObjectRowFixture.objects = businessObjects;
			fixture.DoTables(tables);

			Assert.AreEqual(right, fixture.Counts.Right);
			Assert.AreEqual(wrong, fixture.Counts.Wrong);
			Assert.AreEqual(ignores, fixture.Counts.Ignores);
			Assert.AreEqual(exceptions, fixture.Counts.Exceptions);
		}

		[Test]
		public void TestSurplus()
		{
			TestUtils.InitAssembliesAndNamespaces();
			Fixture fixture = new Fixture();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td>BusinessObjectRowFixture</td></tr>");
			builder.Append("<tr><td>GetFirstString</td></tr>");
			builder.Append("<tr><td>number1</td></tr>");
			builder.Append("</table>");
			Parse parse = new Parse(builder.ToString());

			BusinessObjectRowFixture.objects = new object[]
				{
					new BusinessObject(new string[] {"number1"}),
					new BusinessObject(new string[] {"number2"}),
					new BusinessObject(new string[] {"number3"})
				};

			fixture.DoTables(parse);
			Assert.IsTrue(parse.ToString().IndexOf("number1") > 0);
			Assert.IsTrue(parse.ToString().IndexOf("number2") > 0);
			Assert.IsTrue(parse.ToString().IndexOf("number3") > 0);
			RowFixture rowFixture = (RowFixture) Fixture.LastFixtureLoaded;
			Assert.AreEqual(1, rowFixture.Counts.Right);
			Assert.AreEqual(2, rowFixture.Counts.Wrong);
		}

		[Test]
		public void TestMissing()
		{
			TestUtils.InitAssembliesAndNamespaces();
			Fixture fixture = new Fixture();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td>BusinessObjectRowFixture</td></tr>");
			builder.Append("<tr><td>GetFirstString</td></tr>");
			builder.Append("<tr><td>number1</td></tr>");
			builder.Append("<tr><td>number2</td></tr>");
			builder.Append("<tr><td>number3</td></tr>");
			builder.Append("</table>");
			Parse parse = new Parse(builder.ToString());

			BusinessObjectRowFixture.objects = new object[]
				{
					new BusinessObject(new string[] {"number1"}),
				};

			fixture.DoTables(parse);
			Assert.IsTrue(parse.ToString().IndexOf("number1") > 0);
			Assert.IsTrue(parse.ToString().IndexOf("number2") > 0);
			Assert.IsTrue(parse.ToString().IndexOf("number3") > 0);
			RowFixture rowFixture = (RowFixture) Fixture.LastFixtureLoaded;
			Assert.AreEqual(2, rowFixture.Counts.Wrong);
		}

		[Test]
		public void TestStartsWithHandlerInSecondColumn()
		{
			TestUtils.InitAssembliesAndNamespaces();
			ObjectFactory.AddNamespace("fitnesse.Handlers");
			CellOperation.LoadHandler(new StartsWithHandler());
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td>people row fixture</td></tr>");
			builder.Append("<tr><td>first name</td><td>last name</td></tr>");
			builder.Append("<tr><td>Nigel</td><td>Tuf..</td></tr>");
			builder.Append("</table>");
			PeopleLoaderFixture.people.Clear();
			PeopleLoaderFixture.people.Add(new Person("Nigel", "Tufnel"));
			Parse tables = new Parse(builder.ToString());
			Fixture fixture = new Fixture();
			fixture.DoTables(tables);
			Assert.IsTrue(tables.ToString().IndexOf("Tuf..") > -1);
			Assert.IsFalse(tables.ToString().IndexOf("Tufnel") > -1);
			Fixture peopleRowFixture = Fixture.LastFixtureLoaded;
			Assert.AreEqual(2, peopleRowFixture.Counts.Right);
			Assert.AreEqual(0, peopleRowFixture.Counts.Wrong);
			Assert.AreEqual(0, peopleRowFixture.Counts.Ignores);
			Assert.AreEqual(0, peopleRowFixture.Counts.Exceptions);
		}
		private string rowFixtureName = typeof(NewRowFixtureDerivative).Name;
		private Parse table;
		private Fixture fixture;

		[SetUp]
		public void SetUp()
		{
			TestUtils.InitAssembliesAndNamespaces();
			CellOperation.LoadDefaultHandlers();
			table = new Parse("<table><tr><td>" + rowFixtureName + "</td></tr><tr><td>name</td></tr></table>");
			fixture = new Fixture();
			NewRowFixtureDerivative.QueryValues.Clear();
		}

		[Test]
		public void TestZeroExpectedZeroActual()
		{
			fixture.DoTables(table);
			VerifyCounts(0, 0, 0, 0);
		}

		[Test]
		public void TestOneExpectedOneActualCorrect()
		{
			string name = "Joe";
			AddQueryValue(new RowFixturePerson(name));
			AddRow(new string[]{name});
			fixture.DoTables(table);
			VerifyCounts(1, 0, 0, 0);
			AssertTextInTag(table.At(0,2,0), "pass");
		}

		[Test]
		public void TestOneExpectedOneActualCorrectTwoColumns()
		{
			AddColumn(table, "address");
			string name = "Joe";
			string address = "First Street";
			AddQueryValue(new RowFixturePerson(name, address));
			AddRow(new string[] {name, address});
			fixture.DoTables(table);
			VerifyCounts(2, 0, 0, 0);
			AssertTextInTag(table.At(0,2,0), "pass");
			AssertTextInTag(table.At(0,2,1), "pass");
		}

		[Test]
		public void TestTwoColumnAsKeyAllCorrect()
		{
			AddColumn(table, "address");
			AddColumn(table, "phone?");
			AddRow(new string[] {"Joe", "First Street", "123-1234"});
			AddRow(new string[] {"Joe", "Second Street", "234-2345"});
			AddQueryValue(new RowFixturePerson("Joe", "First Street", "123-1234"));
			AddQueryValue(new RowFixturePerson("Joe", "Second Street", "234-2345"));
			fixture.DoTables(table);
			VerifyCounts(6, 0, 0, 0);
		}

		[Test]
		public void TestTwoColumnAsKeyThirdColumnIncorrect()
		{
			AddColumn(table, "address");
			AddColumn(table, "phone?");
			AddRow(new string[] {"Joe", "First Street", "123-1234"});
			AddRow(new string[] {"Joe", "Second Street", "234-2345"});
			AddQueryValue(new RowFixturePerson("Joe", "First Street", "123-1234"));
			AddQueryValue(new RowFixturePerson("Joe", "Second Street", "234-2346"));
			fixture.DoTables(table);
			VerifyCounts(5, 1, 0, 0);
		}

		[Test]
		public void TestOneExpectedOneActualCorrectTwoColumnsSecondColumnWrong()
		{
			AddColumn(table, "address?");
			string name = "Joe";
			AddQueryValue(new RowFixturePerson(name, "First Street"));
			AddRow(new string[] {name, "Second Street"});
			fixture.DoTables(table);
			VerifyCounts(1, 1, 0, 0);
			AssertTextInTag(table.At(0,2,0), "pass");
			AssertTextInTag(table.At(0,2,1), "fail");
		}

		[Test]
		public void TestOneExpectedOneActualIncorrect()
		{
			AddQueryValue(new RowFixturePerson("Joe"));
			AddRow(new string[] {"John"});
			fixture.DoTables(table);
			VerifyCounts(0, 2, 0, 0);
			AssertTextInTag(table.At(0,2,0), "fail");
			AssertTextInBody(table.At(0,2,0), "missing");
			AssertTextInTag(table.At(0,3,0), "fail");
			AssertTextInBody(table.At(0,3,0), "surplus");
		}

		[Test]
		public void TestTwoExpectedTwoActualAllCorrectOrderCorrect()
		{
			AddQueryValue(new RowFixturePerson("Joe"));
			AddQueryValue(new RowFixturePerson("Jane"));
			AddRow(new string[]{"Joe"});
			AddRow(new string[]{"Jane"});
			fixture.DoTables(table);
			VerifyCounts(2, 0, 0, 0);
		}

		[Test]
		public void TestTwoExpectedTwoActualAllCorrectOrderIncorrect()
		{
			AddQueryValue(new RowFixturePerson("Joe"));
			AddQueryValue(new RowFixturePerson("Jane"));
			AddRow(new string[]{"Jane"});
			AddRow(new string[]{"Joe"});
			fixture.DoTables(table);
			VerifyCounts(2, 0, 0, 0);
		}

		[Test]
		public void TestTwoExpectedTwoActualOneCorrect()
		{
			AddQueryValue(new RowFixturePerson("Joe"));
			AddQueryValue(new RowFixturePerson("Jane"));
			AddRow(new string[]{"Joe"});
			AddRow(new string[]{"Susan"});
			fixture.DoTables(table);
			VerifyCounts(1, 2, 0, 0);
		}

		[Test]
		public void TestTwoExpectedTwoActualOneCorrectOrderIncorrect()
		{
			AddQueryValue(new RowFixturePerson("Joe"));
			AddQueryValue(new RowFixturePerson("Jane"));
			AddRow(new string[]{"Susan"});
			AddRow(new string[]{"Joe"});
			fixture.DoTables(table);
			VerifyCounts(1, 2, 0, 0);
		}

		[Test]
		public void TestOneMissing()
		{
			AddRow(new string[]{"Joe"});
			fixture.DoTables(table);
			VerifyCounts(0, 1, 0, 0);
			AssertTextInTag(table.At(0,2,0), "fail");
			AssertTextInBody(table.At(0,2,0), "missing");
		}

		[Test]
		public void TestOneMissingTwoColumns()
		{
			AddColumn(table, "address");
			AddRow(new string[] {"Joe", "First Street"});
			fixture.DoTables(table);
			VerifyCounts(0, 1, 0, 0);
			AssertTextInTag(table.At(0,2,0), "fail");
			AssertTextInBody(table.At(0,2,0), "missing");
		}

		[Test]
		public void TestOnePresentOneMissingTwoColumns()
		{
			AddColumn(table, "address");
			AddRow(new string[]{"Lilian", "First Street"});
			AddRow(new string[]{"Joe", "Second Street"});
			AddQueryValue(new RowFixturePerson("Lilian", "First Street"));
			fixture.DoTables(table);
			VerifyCounts(2, 1, 0, 0);
			AssertTextInTag(table.At(0,2,0), "pass");
			AssertTextInTag(table.At(0,2,1), "pass");
			AssertTextNotInBody(table.At(0,2,0), "missing");
			AssertTextInTag(table.At(0,3,0), "fail");
			AssertTextInBody(table.At(0,3,0), "missing");
		}

		[Test]
		public void TestOnePresentOneMissingTwoColumnsReverseOrder()
		{
			AddColumn(table, "address");
			AddRow(new string[]{"Joe", "Second Street"});
			AddRow(new string[]{"Lilian", "First Street"});
			AddQueryValue(new RowFixturePerson("Lilian", "First Street"));
			fixture.DoTables(table);
			VerifyCounts(2, 1, 0, 0);
			AssertTextInTag(table.At(0,2,0), "fail");
			AssertTextInBody(table.At(0,2,0), "missing");
			AssertTextNotInBody(table.At(0,2,1), "missing");
			AssertTextInTag(table.At(0,3,0), "pass");
			AssertTextInTag(table.At(0,3,1), "pass");
			AssertTextNotInBody(table.At(0,3,0), "missing");
			AssertTextNotInBody(table.At(0,3,1), "missing");
		}

		[Test]
		public void TestCorrectFormatForMissing()
		{
			string loaderFixtureHtml = "<table>" +
				"<tr><td colspan=\"3\">people loader fixture</td></tr>" +
				"<tr><td>id</td><td>first name</td><td>last name</td></tr>" +
				"<tr><td>1</td><td>null</td><td>Jones</td></tr>" +
				"<tr><td>2</td><td>Phil</td><td>blank</td></tr>" +
				"</table>";
			string inspectorFixtureHtml = "<table>" +
				"<tr><td colspan=\"3\">people row fixture</td></tr>" +
				"<tr><td>id</td><td>first name</td><td>last name</td></tr>" +
				"<tr><td>7</td><td>nullest</td><td>Jonesey</td></tr>" +
				"<tr><td>2</td><td>Phil</td><td>blank</td></tr>" +
				"</table>";
			string processedInspectorFixtureHtml = "<table>" +
				"<tr><td colspan=\"3\">people row fixture</td></tr>" +
				"<tr><td>id</td><td>first name</td><td>last name</td></tr>" +
				"<tr><td class=\"fail\">7 <span class=\"fit_label\">missing</span></td><td>nullest</td><td>Jonesey</td></tr>" +
				"<tr><td class=\"pass\">2</td><td class=\"pass\">Phil</td><td class=\"pass\">blank</td></tr>" +
				"\n<tr>\n<td class=\"fail\"> <span class=\"fit_grey\">1</span> <span class=\"fit_label\">surplus</span></td>\n<td> <span class=\"fit_grey\">null</span></td>\n<td> <span class=\"fit_grey\">Jones</span></td></tr>" +
				"</table>";
			Parse tables = new Parse(loaderFixtureHtml + inspectorFixtureHtml);
			Fixture fixture = new Fixture();
			fixture.DoTables(tables);
			Assert.AreEqual(loaderFixtureHtml + processedInspectorFixtureHtml, tables.ToString());
		}

		[Test]
		public void TestArrayOfStrings()
		{
			string setUpTableHtml = "<table>" +
				"<tr><td colspan=\"3\">ArrayOfStringsFixture</td></tr>" +
				"<tr><td>field</td><td>save!</td></tr>" +
				"<tr><td>a,b,c</td><td></td></tr>" +
				"</table>";
			string processedSetUpTableHtml = "<table>" +
				"<tr><td colspan=\"3\">ArrayOfStringsFixture</td></tr>" +
				"<tr><td>field</td><td>save!</td></tr>" +
				"<tr><td>a,b,c</td><td> <span class=\"fit_grey\">null</span></td></tr>" +
				"</table>";
			string tableHtml = "<table>" +
				"<tr><td colspan=\"3\">ArrayOfStringsRowFixture</td></tr>" +
				"<tr><td>field</td></tr>" +
				"<tr><td>a,b,c</td></tr>" +
				"</table>";
			string expected = "<table>" +
				"<tr><td colspan=\"3\">ArrayOfStringsRowFixture</td></tr>" +
				"<tr><td>field</td></tr>" +
				"<tr><td class=\"pass\">a,b,c</td></tr>" +
				"</table>";
			Fixture fixture = new Fixture();
			Parse tables = new Parse(setUpTableHtml + tableHtml);
			fixture.DoTables(tables);
			Assert.AreEqual(processedSetUpTableHtml + expected, tables.ToString());
		}

		[Test]
		public void TestEnum()
		{
			string tableHtml = "<table><tr><td>ColorInspector</td></tr>" +
				"<tr><td>ToString()</td></tr>" +
				"<tr><td>Red</td></tr>" +
				"<tr><td>Blue</td></tr>" +
				"</table>";
			Array colorsArray = Enum.GetValues(typeof(Color));
			ArrayList colorsList = new ArrayList(colorsArray);
			DoTable(new Parse(tableHtml), colorsList.ToArray(), 2, 0, 0, 0);
		}

		private void VerifyCounts(int right, int wrong, int exceptions, int ignores)
		{
			Assert.AreEqual(right, fixture.Counts.Right);
			Assert.AreEqual(wrong, fixture.Counts.Wrong);
			Assert.AreEqual(exceptions, fixture.Counts.Exceptions);
			Assert.AreEqual(ignores, fixture.Counts.Ignores);
		}

		private void AddQueryValue(object obj)
		{
			NewRowFixtureDerivative.QueryValues.Add(obj);
		}

		private void AddRow(string[] strings)
		{
			Parse lastCell = new Parse("td", strings[strings.Length - 1], null, null);
			for (int i = strings.Length - 1; i > 0; i--)
			{
				lastCell = new Parse("td", strings[i - 1], null, lastCell);
			}
			table.Parts.Last.More = new Parse("tr", null, lastCell, null);
		}

		private void AssertTextInTag(Parse cell, string text)
		{
			Assert.IsTrue(cell.Tag.IndexOf(text) > -1);
		}

		private void AssertTextInBody(Parse cell, string text)
		{
			Assert.IsTrue(cell.Body.IndexOf(text) > -1);
		}

		private void AssertTextNotInBody(Parse cell, string text)
		{
			Assert.IsFalse(cell.Body.IndexOf(text) > -1);
		}

		private void AddColumn(Parse table, string name)
		{
			table.Parts.More.Parts.Last.More = new Parse("td", name, null, null);
		}
	}

	public class BusinessObject
	{
		private string[] strs;

		public BusinessObject(string[] strs)
		{
			this.strs = strs;
		}

		public string[] GetStrings()
		{
			return strs;
		}

		public string GetFirstString()
		{
			return strs[0];
		}
	}

	public class BusinessObjectRowFixture : RowFixture
	{
		public static object[] objects;

		public override object[] Query()
		{
			return objects;
		}

		public override Type GetTargetClass()
		{
			return typeof (BusinessObject);
		}
	}

	public class NewRowFixtureDerivative : RowFixture
	{
		public static ArrayList QueryValues = new ArrayList();

		public override object[] Query()
		{
			return QueryValues.ToArray();
		}

		public override Type GetTargetClass()
		{
			return typeof(RowFixturePerson);
		}
	}

	public class RowFixturePerson
	{
		public RowFixturePerson(string name)
		{
			this.name = name;
		}

		public RowFixturePerson(string name, string address)
		{
			this.name = name;
			this.address = address;
		}

		public RowFixturePerson(string name, string address, string phone)
		{
			this.name = name;
			this.address = address;
			this.phone = phone;
		}

		public string Name
		{
			get { return name; }
		}

		public string Address
		{
			get { return address; }
		}

		public string Phone
		{
			get { return phone; }
		}

		private string name;
		private string address;
		private string phone;
	}

	public class ColorInspectorFixture : RowFixture
	{
		public override object[] Query()
		{
			Array colorsArray = Enum.GetValues(typeof(Color));
			ArrayList colorsList = new ArrayList(colorsArray);
			return colorsList.ToArray();
		}

		public override Type GetTargetClass()
		{
			return typeof(Color);
		}
	}
}