// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class ImportFixtureTest
	{
		[SetUp]
		public void SetUp()
		{
			TestUtils.InitAssembliesAndNamespaces();
			ObjectFactory.Namespaces = new ArrayList();
			Fixture.ResetNamespaces();
		}

		[Test]
		public void TestInitialState() {
			Assert.AreEqual(2, ObjectFactory.Namespaces.Count);
			Assert.IsTrue(ObjectFactory.Namespaces.Contains("fit"));
		}
		[Test]
		public void TestImport() {
			string firstTable = "<table>" +
				"<tr><td>Import</td></tr>" +
				"<tr><td>fitTest</td></tr>" +
				"</table>";
			string secondTable = "<table>" +
				"<tr><td>NamespaceInspector</td></tr>" +
				"<tr><td>namespace</td></tr>" +
				"<tr><td>fit</td></tr>" +
				"<tr><td>fitnesse.handlers</td></tr>" +
				"<tr><td>fitTest</td></tr>" +
				"</table>";
			Parse parse = new Parse(firstTable + secondTable);
			Fixture fixture = new Fixture();
			Assert.AreEqual(2, ObjectFactory.Namespaces.Count);
			fixture.DoTables(parse);
			Assert.AreEqual(3, ObjectFactory.Namespaces.Count);
			Assert.IsTrue(ObjectFactory.Namespaces.Contains("fitTest"));
			Assert.AreEqual(3, Fixture.LastFixtureLoaded.Counts.Right);
		
		}
	}

	public class NamespaceInspector : RowFixture {
		public override object[] Query() {
			object[] values = new object[ObjectFactory.Namespaces.Count];
			for (int i = 0; i < values.Length; i++) {
				values[i] = new NamespaceWrapper((string) ObjectFactory.Namespaces[i]);
			}
			return values;
		}

		public override Type GetTargetClass() {
			return typeof(NamespaceWrapper);
		}

	}

	public class NamespaceWrapper
	{
		public NamespaceWrapper(string name)
		{
			this.name = name;
		}

		public string Namespace
		{
			get { return name; }
		}	

		private string name;
	}

}

namespace fitTest
{
	
}
