// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Reflection;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class AccessorTest
	{
		private Fixture fixture;
		private FieldInfo fieldInfo;
		private PropertyInfo propertyInfo;
		private MethodInfo setMethodInfo;
		private MethodInfo getMethodInfo;
		private Accessor fieldAccessor;
		private Accessor propertyAccessor;
		private Accessor setMethodAccessor;
		private Accessor getMethodAccessor;

		private void Init(Fixture fixture)
		{
			this.fixture = fixture;
			fieldInfo = fixture.GetType().GetField("Field");
			fieldAccessor = new FieldAccessor(fieldInfo);
			propertyInfo = fixture.GetType().GetProperty("Property");
			propertyAccessor = new PropertyAccessor(propertyInfo);
			setMethodInfo = fixture.GetType().GetMethod("Set");
			setMethodAccessor = new MethodAccessor(setMethodInfo);
			getMethodInfo = fixture.GetType().GetMethod("Get");
			getMethodAccessor = new MethodAccessor(getMethodInfo);
		}

		[Test]
		public void TestIntClass()
		{
			Init(new IntFixture());
			VerifyValues(typeof (int), new object[] {7, 3, 2}, new string[] {"7", "3", "2"});
		}

		[Test]
		public void TestFloatClass()
		{
			Init(new FloatFixture());
			VerifyValues(typeof (float), new object[] {7.23f, 3.17f, 2.89f}, new string[] {"7.23", "3.17", "2.89"});
		}

		[Test]
		public void TestDecimalClass()
		{
			Init(new DecimalFixture());
			VerifyValues(typeof (decimal), new object[] {7.12m, 3.29m, 2.5789m}, new string[] {"7.12", "3.29", "2.5789"});
		}

		[Test]
		public void TestLongClass()
		{
			Init(new LongFixture());
			string[] expected = new string[] {"123456789", "987654321", "1"};
			object[] actual = new object[] {123456789L, 987654321L, 1L};
			VerifyValues(typeof (long), actual, expected);
		}

		[Test]
		public void TestDoubleClass()
		{
			Init(new DoubleFixture());
			string[] expected = new string[] {"1.23", "4.567", "987.45"};
			object[] actual = new object[] {1.23, 4.567, 987.45};
			VerifyValues(typeof (double), actual, expected);
		}

		[Test]
		public void TestArrayOfIntsClass()
		{
			Init(new ArrayOfIntsFixture());
			VerifyValues(
				typeof (int[]),
				new object[]
					{
						new int[] {1, 2, 3},
						new int[] {4, 5, 6},
						new int[] {7, 8, 9}
					},
				new string[]
					{
						"1,2,3",
						"4,5,6",
						"7,8,9"
					});
		}

		[Test]
		public void TestArrayOfStrings()
		{
			Init(new ArrayOfStringsFixture());
			VerifyValues(
				typeof (string[]),
				new object[]
					{
						new string[] {"a", "b", "c"},
						new string[] {"a", "b", "c"},
						new string[] {"a", "b", "c"}
					},
				new string[]
					{
						"a,b,c",
						"a,b,c",
						"a,b,c"
					});
		}

		[Test]
		public void TestCustomType()
		{
			Init(new PersonFixture());
			VerifyValues(
				typeof (Person),
				new object[]
					{
						new Person("john", "doe"),
						new Person("john", "doe"),
						new Person("john", "doe")
					},
				new string[]
					{
						"john doe",
						"john doe",
						"john doe"
					});
		}

		private void VerifyValues(Type type, object[] actualValues, string[] assertedValues)
		{
			TypeAdapter adapter = new TypeAdapter(type);
			fieldAccessor.Set(fixture, actualValues[0]);
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse(assertedValues[0]), fieldAccessor.Get(fixture)));
			propertyAccessor.Set(fixture, actualValues[1]);
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse(assertedValues[1]), propertyAccessor.Get(fixture)));
			setMethodAccessor.Set(fixture, actualValues[2]);
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse(assertedValues[2]), getMethodAccessor.Get(fixture)));
		}
	}
}