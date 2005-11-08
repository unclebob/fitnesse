// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class TypeNameTest
	{
		[Test]
		public void TestGetNameNoNamespace()
		{
			TypeName name = new TypeName("SomeFixture");
			Assert.AreEqual("SomeFixture", name.Name);
		}

		[Test]
		public void TestGetNameWithOnePartNamespace()
		{
			TypeName name = new TypeName("SomeNamespace.SomeFixture");
			Assert.AreEqual("SomeFixture", name.Name);
		}

		[Test]
		public void TestGetNamespaceWithOnePartNamespace()
		{
			TypeName name = new TypeName("SomeNamespace.SomeFixture");
			Assert.AreEqual("SomeNamespace", name.Namespace);
		}

		[Test]
		public void TestGetNameWithTwoPartNamespace()
		{
			TypeName name = new TypeName("Prefix.SomeNamespace.SomeFixture");
			Assert.AreEqual("SomeFixture", name.Name);
		}

		[Test]
		public void TestGetNamespaceWithTwoPartNamespace()
		{
			TypeName name = new TypeName("Prefix.SomeNamespace.SomeFixture");
			Assert.AreEqual("Prefix.SomeNamespace", name.Namespace);
		}

		[Test]
		public void TestIsFullyQualified()
		{
			Assert.IsTrue(new TypeName("this.is.FullyQualified").IsFullyQualified());
			Assert.IsTrue(new TypeName("this.is.Fully_Qualified").IsFullyQualified());
			Assert.IsFalse(new TypeName("thisIsNotFullyQualified....").IsFullyQualified());
			Assert.IsFalse(new TypeName("this . is . not . fully . qualified").IsFullyQualified());
		}

		[Test]
		public void IsFullyQualifiedShouldAllowNumbers()
		{
			Assert.IsTrue(new TypeName("This1.Is2.FullyQualified3").IsFullyQualified());
		}

		[Test]
		public void IsFullyQualifiedShouldNotAllowNumbersAtTheBeginningOfAnyPartial()
		{
			Assert.IsFalse(new TypeName("1This.Is.FullyQualified").IsFullyQualified());
			Assert.IsFalse(new TypeName("This.2Is.FullyQualified").IsFullyQualified());
			Assert.IsFalse(new TypeName("This.Is.3FullyQualified").IsFullyQualified());
		}

		[Test]
		public void TestOriginalName()
		{
			string originalName = "This.is.the orginal name!!!?";
			TypeName name = new TypeName(originalName);
			Assert.AreEqual(originalName, name.OriginalName);
		}
	}
}