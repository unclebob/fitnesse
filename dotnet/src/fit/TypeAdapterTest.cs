// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using NUnit.Framework;
using fit;

namespace fit
{
	[TestFixture]
	public class TypeAdapterTest
	{
		TypeAdapter adapter;

		[Test]
		public void TestIntAdapter()
		{
			adapter = new TypeAdapter(typeof (int));
			Assert.AreEqual(123, adapter.Parse("123"));
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse("123"), 123));
		}

		[Test]
		public void TestFloatAdapter()
		{
			adapter = new TypeAdapter(typeof (float));
			Assert.AreEqual(12.3f, (float) adapter.Parse("12.3"), .00001);
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse("12.3"), 12.3f));
		}

		[Test]
		public void TestDoubleAdapter()
		{
			adapter = new TypeAdapter(typeof (double));
			Assert.AreEqual(12.3, (double) adapter.Parse("12.3"), .00001);
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse("12.3"), 12.3));
		}

		[Test]
		public void TestDecimalAdapter()
		{
			adapter = new TypeAdapter(typeof (decimal));
			Assert.AreEqual(12.3M, (decimal) adapter.Parse("12.3"));
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse("12.3"), 12.3M));
		}

		[Test]
		public void TestLongAdapter()
		{
			adapter = new TypeAdapter(typeof (long));
			Assert.AreEqual(123L, (long) adapter.Parse("123"));
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse("123"), 123L));
		}

		[Test]
		public void TestStringAdapter()
		{
			adapter = new TypeAdapter(typeof (string));
			Assert.AreEqual("123", (string) adapter.Parse("123"));
			Assert.IsTrue(TypeAdapter.AreEqual(adapter.Parse("123"), "123"));
		}

		[Test]
		public void TestStringArrayAdapter()
		{
			adapter = new TypeAdapter(typeof (string[]));
			Assert.IsTrue(TypeAdapter.AreEqual(new object[] {"1", "2", "3"}, (object[]) adapter.Parse("1,2,3")));
		}

		[Test]
		public void TestStringArrayEquality()
		{
			string[] array1 = {"a", "b", "c"};
			string[] array2 = {"a", "b", "c"};
			string[] array3 = {"d", "e"};
			Assert.IsTrue(TypeAdapter.AreEqual(array1, array2));
			Assert.IsFalse(TypeAdapter.AreEqual(array1, array3));
			Assert.IsFalse(TypeAdapter.AreEqual(array2, array3));
		}

		[Test]
		public void TestIntArrayAdapter()
		{
			adapter = new TypeAdapter(typeof (int[]));
			Assert.IsTrue(TypeAdapter.AreEqual(new object[] {1, 2, 3}, (Array) adapter.Parse("1,2,3")));
		}

		[Test]
		public void TestIntArrayEquality()
		{
			int[] array1 = {1, 2, 3};
			int[] array2 = {1, 2, 3};
			int[] array3 = {4, 5};
			int[] array4 = {6, 7, 8};
			Assert.IsTrue(TypeAdapter.AreEqual(array1, array2));
			Assert.IsFalse(TypeAdapter.AreEqual(array1, array3));
			Assert.IsFalse(TypeAdapter.AreEqual(array2, array3));
			Assert.IsFalse(TypeAdapter.AreEqual(array1, array4));
		}
	}
}