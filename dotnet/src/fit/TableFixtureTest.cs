// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class TableFixtureTest
	{
		private string table;
		private Fixture fixture;

		[SetUp]
		public void SetUp()
		{
			TestUtils.InitAssembliesAndNamespaces();
			StringBuilder builder = new StringBuilder();
			builder.Append("<table>");
			builder.Append("<tr><td colspan='5'>ExampleTableFixture</td></tr>");
			builder.Append("<tr><td>0,0</td><td>0,1</td><td>0,2</td><td>37</td><td></td></tr>");
			builder.Append("</table>");
			table = builder.ToString();
			fixture = new Fixture();
			fixture.Listener = new SimpleFixtureListener();
			fixture.DoTables(new Parse(table));
		}

		[TearDown]
		public void TearDown()
		{
			ExampleTableFixture.ResetStatics();
		}

		private class SimpleFixtureListener : FixtureListener
		{
			private Parse finishedTable;

			public Parse FinishedTable
			{
				get { return finishedTable; }
				set { finishedTable = value; }
			}

			public void TableFinished(Parse finishedTable)
			{
				this.finishedTable = finishedTable;
			}

			public void TablesFinished(Counts counts)
			{
			}
		}

		private Parse getFinishedTable()
		{
			return ((SimpleFixtureListener)fixture.Listener).FinishedTable;
		}

		[Test]
		public void TestNumRows()
		{
			Assert.AreEqual(1, ExampleTableFixture.numRows);
		}

		[Test]
		public void TestDoStaticTable()
		{
			Assert.AreEqual(1, ExampleTableFixture.timesDoStaticTableCalled);
		}

		[Test]
		public void TestGetCell()
		{
			Assert.AreEqual("0,0", ExampleTableFixture.ZeroZero);
		}

		[Test]
		public void TestGetText()
		{
			Assert.AreEqual("0,1", ExampleTableFixture.ZeroOne);
		}

		[Test]
		public void TestGetInt()
		{
			Assert.AreEqual(37, ExampleTableFixture.resultOfGetInt);
		}

		[Test]
		public void TestRight()
		{
			Assert.AreEqual(1, fixture.Counts.Right);
			Assert.IsTrue(getFinishedTable().At(0,1,0).Tag.IndexOf("class=\"pass\"") > 0);
		}

		[Test]
		public void TestWrong()
		{
			Assert.AreEqual(2, fixture.Counts.Wrong);
			Assert.IsTrue(getFinishedTable().At(0,1,1).Tag.IndexOf("class=\"fail\"") > 0);
			Assert.IsTrue(getFinishedTable().At(0,1,2).Tag.IndexOf("class=\"fail\"") > 0);
			Assert.IsTrue(getFinishedTable().At(0,1,2).Text.IndexOf("actual") > 0);
		}

		[Test]
		public void TestIgnore()
		{
			Assert.AreEqual(1, fixture.Counts.Ignores);
			Assert.IsTrue(getFinishedTable().At(0,1,3).Tag.IndexOf("class=\"ignore\"") > 0);
		}

		[Test]
		public void TestBlank()
		{
			Assert.IsTrue(ExampleTableFixture.blankCell);
		}
	}

	public class ExampleTableFixture : TableFixture
	{
		public static int timesDoStaticTableCalled;
		public static int numRows;
		public static string ZeroZero;
		public static string ZeroOne;
		public static int resultOfGetInt;
		public static bool blankCell;

		public static void ResetStatics()
		{
			timesDoStaticTableCalled = 0;
			numRows = 0;
			ZeroZero = null;
			ZeroOne = null;
			resultOfGetInt = 0;
			blankCell = false;
		}

		protected override void DoStaticTable(int rows)
		{
			timesDoStaticTableCalled++;
			numRows = rows;
			ZeroZero = GetCell(0,0).Text;
			ZeroOne = GetString(0,1);
			Right(0,0);
			Wrong(0,1);
			Wrong(0,2,"0,3");
			Ignore(0,3);
			resultOfGetInt = GetInt(0,3);
			blankCell = Blank(0,4);
		}
	}
}