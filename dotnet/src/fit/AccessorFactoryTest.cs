// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class AccessorFactoryTest
	{
		[Test]
		public void TestCreateAccessorForFieldWithPreciseName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "TheField");
			Assert.IsTrue(accessor is FieldAccessor);
		}

		[Test]
		public void TestCreateAccessorForFieldWithGracefulName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "the field");
			Assert.IsTrue(accessor is FieldAccessor);
		}

		[Test]
		public void TestCreateAccessorForPropertyWithPreciseName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "TheProperty");
			Assert.IsTrue(accessor is PropertyAccessor);
		}

		[Test]
		public void TestCreateAccessorForPropertyWithGracefulName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "the property");
			Assert.IsTrue(accessor is PropertyAccessor);
		}

		[Test]
		public void TestCreateAccessorForGetMethodWithPreciseName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "TheGetterMethod");
			Assert.IsTrue(accessor is MethodAccessor);
		}

		[Test]
		public void TestCreateAccessorForGetMethodWithGracefulName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "the geTTer method");
			Assert.IsTrue(accessor is MethodAccessor);
		}

		[Test]
		public void TestCreateAccessorForSetMethodWithPreciseName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "TheSetterMethod");
			Assert.IsTrue(accessor is MethodAccessor);
		}

		[Test]
		public void TestCreateAccessorForSetMethodWithGracefulName() {
			Accessor accessor = AccessorFactory.Create(typeof (MemberFinderTestFixture), "the setter method");
			Assert.IsTrue(accessor is MethodAccessor);
		}
	}

	public class MemberFinderTestFixture : ColumnFixture
	{
		public string TheField;

		public string TheProperty
		{
			get { return TheField; }
			set { this.TheField = value; }
		}

		public string TheGetterMethod()
		{
			return TheField;
		}

		public void TheSetterMethod(string value)
		{
			this.TheField = value;
		}
	}
}