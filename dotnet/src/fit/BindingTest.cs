// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using NUnit.Framework;

namespace fit
{
	[TestFixture]
	public class BindingTest
	{
		//TODO - move me to OperationTypeSelectorTest
		[Test]
		public void TestIsQueryCell() 
		{
			ColumnFixture textFixture = new TestFixture();
			Assert.IsTrue(textFixture.CheckIsImpliedBy("isQuery?"));
			Assert.IsTrue(textFixture.CheckIsImpliedBy("isQuery!"));
			Assert.IsTrue(textFixture.CheckIsImpliedBy("isQuery()"));
			Assert.IsFalse(textFixture.CheckIsImpliedBy("isNotQuery+"));
			Assert.IsFalse(textFixture.CheckIsImpliedBy("isNotQuery*"));
			Assert.IsFalse(textFixture.CheckIsImpliedBy("isNotQuery<>"));
		}

		[Test]
		public void TestSetterBinding()
		{
			TestFixture f = new TestFixture ();
			
			Parse p;
			Binding binding;

			binding = new Binding("sampleInt", OperationType.Input);
			p = new Parse("<table><tr><td>123456</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual(123456, f.sampleInt);

			p = new Parse("<table><tr><td>-234567</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual(-234567, f.sampleInt);
			
			binding = new Binding("sampleDouble", OperationType.Input);
			p = new Parse("<table><tr><td>3.14159</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual(3.14159, f.sampleDouble);

			binding = new Binding("sampleChar", OperationType.Input);
			p = new Parse("<table><tr><td>a</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual('a', f.sampleChar);

			binding = new Binding("sampleString", OperationType.Input);
			p = new Parse("<table><tr><td>xyzzy</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual("xyzzy", f.sampleString);

			binding = new Binding("sampleFloat", OperationType.Input);
			p = new Parse("<table><tr><td>6.02e23</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual(6.02e23f, f.sampleFloat, 1e17f);

			binding = new Binding("sampleByte", OperationType.Input);
			p = new Parse("<table><tr><td>123</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual((byte)123, f.sampleByte);

			binding = new Binding("sampleShort", OperationType.Input);
			p = new Parse("<table><tr><td>12345</td></tr></table>").Parts.Parts;
			binding.HandleCell(f, p);
			Assert.AreEqual((short)12345, f.sampleShort);
		}

		class TestFixture : ColumnFixture 
		{
			public byte sampleByte = 0;
			public short sampleShort =0;
			public int sampleInt = 0;
			public float sampleFloat = 0;
			public double sampleDouble = 3.14159862;
			public char sampleChar = '\0';
			public string sampleString = null;
			public int[] sampleArray = null;
			public DateTime sampleDate = DateTime.Now;
		}

	}
}
