// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using System.Reflection;
using System.Text;
using NUnit.Framework;

namespace fit
{
	public class TestUtils
	{
		public static void InitAssembliesAndNamespaces()
		{
			ObjectFactory.AddAssembly(Assembly.GetAssembly(typeof (TestUtils)).CodeBase);
			ObjectFactory.AddNamespace("fit");
			ObjectFactory.AddNamespace("fitnesse.handlers");
		}
	}

	public class IntFixture : ColumnFixture
	{
		public int Field;
		private int propertyValue;
		private int methodValue;

		public int Property
		{
			get { return propertyValue; }
			set { propertyValue = value; }
		}

		public void Set(int value)
		{
			methodValue = value;
		}

		public int Get()
		{
			return methodValue;
		}
	}

	public class StringFixture : ColumnFixture
	{
		public string Field;
		private string propertyValue;
		private string methodValue;

		public string Property
		{
			get { return propertyValue; }
			set { propertyValue = value; }
		}

		public void Set(string value)
		{
			methodValue = value;
		}

		public string Get()
		{
			return methodValue;
		}
	}

	public class ArrayOfIntsFixture : ColumnFixture
	{
		public int[] Field;
		private int[] propertyValue;
		private int[] methodValue;

		public int[] Property
		{
			set { propertyValue = value; }
			get { return propertyValue; }
		}

		public void Set(int[] value)
		{
			methodValue = value;
		}

		public int[] Get()
		{
			return methodValue;
		}
	}

	public class DoubleFixture : ColumnFixture
	{
		public double Field;
		private double propertyValue;
		private double methodValue;

		public void Set(double value)
		{
			methodValue = value;
		}

		public double Get()
		{
			return methodValue;
		}

		public double Property
		{
			set { propertyValue = value; }
			get { return propertyValue; }
		}
	}

	public class LongFixture : ColumnFixture
	{
		public long Field;
		private long propertyValue;
		private long methodValue;

		public void Set(long value)
		{
			methodValue = value;
		}

		public long Get()
		{
			return methodValue;
		}

		public long Property
		{
			set { propertyValue = value; }
			get { return propertyValue; }
		}
	}

	public class DecimalFixture : ColumnFixture
	{
		public decimal Field;
		private decimal propertyValue;
		private decimal methodValue;

		public void Set(decimal value)
		{
			methodValue = value;
		}

		public decimal Get()
		{
			return methodValue;
		}

		public decimal Property
		{
			set { propertyValue = value; }
			get { return propertyValue; }
		}
	}

	public class FloatFixture : ColumnFixture
	{
		public float Field;
		private float propertyValue;
		private float methodValue;

		public void Set(float value)
		{
			methodValue = value;
		}

		public float Get()
		{
			return methodValue;
		}

		public float Property
		{
			get { return propertyValue; }
			set { propertyValue = value; }
		}
	}

	public class BoolFixture : ColumnFixture
	{
		public bool Field;
		private bool propertyValue;
		private bool methodValue;

		public bool Property
		{
			get { return propertyValue; }
			set { propertyValue = value; }
		}

		public void Set(bool value)
		{
			methodValue = value;
		}

		public bool Get()
		{
			return methodValue;
		}
	}

	public class ArrayOfBoolsFixture : ColumnFixture
	{
		public bool[] Field;
		private bool[] propertyValues;
		private bool[] methodValues;

		public bool[] Property
		{
			set { propertyValues = value; }
			get { return propertyValues; }
		}

		public void Set(bool[] value)
		{
			methodValues = value;
		}

		public bool[] Get()
		{
			return methodValues;
		}
	}

	public class Person
	{
		private int id;
		private string firstName;
		private string lastName;
		private bool talented;

		public int Id
		{
			get { return id; }
		}

		public string FirstName
		{
			get { return firstName; }
		}

		public string LastName
		{
			get { return lastName; }
		}

		public static Person Parse(string name)
		{
			string[] names = name.Split(' ');
			return new Person(names[0], names[1]);
		}

		public Person(string firstName, string lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public Person(int id, string firstName, string lastName) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public override string ToString()
		{
			StringBuilder builder = new StringBuilder(firstName);
			if (builder.Length > 0 && lastName != null && lastName.Length > 0)
			{
				builder.Append(" ");
			}
			return builder.Append(lastName).ToString();
		}

		public override bool Equals(object obj)
		{
			Person that = obj as Person;
			if (that == null)
				return false;
			return this.firstName == that.firstName && this.lastName == that.lastName;
		}

		public override int GetHashCode() {
			return id.GetHashCode() + firstName.GetHashCode() + lastName.GetHashCode();
		}

		public void SetTalented(bool talented)
		{
			this.talented = talented;
		}

		public bool IsTalented
		{
			get { return talented; }
		}
	}

	public class PersonFixture : ColumnFixture
	{
		public Person Field;
		private Person propertyValue;
		private Person methodValue;

		public Person Property
		{
			set { propertyValue = value; }
			get { return propertyValue; }
		}

		public void Set(Person value)
		{
			methodValue = value;
		}

		public Person Get()
		{
			return methodValue;
		}
	}

	public class ArrayOfPeopleFixture : ColumnFixture
	{
		public Person[] Field;
		private Person[] propertyValue;
		private Person[] methodValue;

		public Person[] Property
		{
			set { propertyValue = value; }
			get { return propertyValue; }
		}

		public void Set(Person[] value)
		{
			methodValue = value;
		}

		public Person[] Get()
		{
			return methodValue;
		}
	}

	public class ArrayOfStringsFixture : ColumnFixture
	{
		public string[] Field;

		public void Set(string[] value)
		{
			Field = value;
		}

		public string[] Get()
		{
			return Field;
		}

		public string[] Property
		{
			set { Field = value; }
			get { return Field; }
		}

		public void Save()
		{
			ArrayOfStringsRowFixture.items.Add(this);
		}
	}

	public class ArrayOfStringsRowFixture : RowFixture
	{
		public static ArrayList items = new ArrayList();

		public override object[] Query()
		{
			return items.ToArray();
		}

		public override Type GetTargetClass()
		{
			return typeof(ArrayOfStringsFixture);
		}
	}

	public class ErrorThrowingFixture : ColumnFixture
	{
		public string ErrorThrowingMethod()
		{
			throw new ApplicationException();
		}

		public string ErrorThrowingProperty
		{
			get { throw new ApplicationException(); }
		}

		public string RedirectToErrorThrowingMethod()
		{
			return ErrorThrowingMethod();
		}
	}

	public class ExceptionThrowingFixture : ColumnFixture
	{
		public string Message;
		public string ThrowNullReferenceException()
		{
			throw new NullReferenceException(Message);
		}
		public string ThrowApplicationException()
		{
			throw new ApplicationException(Message);
		}
	}

	[TestFixture]
	public class PersonTest
	{
		[Test]
		public void TestConstructor() {
			Person person = new Person("john", "doe");
			Assert.AreEqual("john doe", person.ToString());
		}

		[Test]
		public void TestConstructorWithId() {
			Person person = new Person(1, "jane", "roe");
			Assert.AreEqual("jane roe", person.ToString());
			Assert.AreEqual("jane", person.FirstName);
			Assert.AreEqual("roe", person.LastName);
			Assert.AreEqual(1, person.Id);
		}

		[Test]
		public void TestIsTalented()
		{
			Person person = new Person("Scott", "Henderson");
			person.SetTalented(true);
			Assert.IsTrue(person.IsTalented);
		}

		[Test]
		public void TestParse()
		{
			string name = "joe schmoe";
			Person person = Person.Parse(name);
			Assert.AreEqual(name, person.ToString());
		}

		[Test]
		public void TestEquals()
		{
			Person original = new Person("Wes", "Montgomery");
			Person copy = new Person("Wes", "Montgomery");
			Assert.IsTrue(original.Equals(copy));
		}
	}

	public class PeopleLoaderFixture : ColumnFixture
	{
		public static ArrayList people = new ArrayList();
		public string FirstName;
		public string LastName;
		public int id;
		public override void Execute() {
			people.Add(new Person(id, FirstName, LastName));
		}
		public string Clear()
		{
			people.Clear();
			return "cleared";
		}
	}

	public class PeopleRowFixture : RowFixture
	{
		public override object[] Query()
		{
			return PeopleLoaderFixture.people.ToArray();
		}

		public override Type GetTargetClass()
		{
			return typeof(Person);
		}
	}

	public enum Color
	{
		Red,
		Blue
	}

}