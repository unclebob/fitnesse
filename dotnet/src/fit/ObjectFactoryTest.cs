// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class ObjectFactoryTest
	{
		private ObjectFactory factory;

		[SetUp]
		public void SetUp()
		{
			TestUtils.InitAssembliesAndNamespaces();
			factory = new ObjectFactory("fixture");
		}

		[Test]
		public void TestIsTypeAvailableTrue()
		{
			Assert.IsTrue(factory.IsTypeAvailable("action"));
			Assert.IsTrue(factory.IsTypeAvailable("actionfixture"));
			Assert.IsTrue(factory.IsTypeAvailable("ActionFixture"));
			Assert.IsTrue(factory.IsTypeAvailable("fit.ActionFixture"));
			Assert.IsTrue(factory.IsTypeAvailable("some class in default name space"));
			Assert.IsTrue(factory.IsTypeAvailable("SomeNamespace.SomeClassInSomeNamespace"));
		}

		[Test]
		public void TestIsTypeAvailableFalse()
		{
			Assert.IsFalse(factory.IsTypeAvailable("SomeClassInSomeNamespace"));
		}

		[Test]
		public void TestFindFixtureSuccess()
		{
			Assert.IsNotNull(factory.CreateInstance("action"));
			Assert.IsNotNull(factory.CreateInstance("actionfixture"));
			Assert.IsNotNull(factory.CreateInstance("ActionFixture"));
			Assert.IsNotNull(factory.CreateInstance("fit.ActionFixture"));
			Assert.IsNotNull(factory.CreateInstance("some class in default name space"));
			Assert.IsNotNull(factory.CreateInstance("SomeNamespace.SomeClassInSomeNamespace"));
		}

		[Test, ExpectedException(typeof (ApplicationException))]
		public void TestFindFixtureFail()
		{
			factory.CreateInstance("SomeClassInSomeNamespace");
		}
	}
}

public class SomeClassInDefaultNamespace : ColumnFixture
{}

namespace SomeNamespace
{
	public class SomeClassInSomeNamespace : ColumnFixture
	{}
}