// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Reflection;

namespace fit
{
	public class TypeAdapter
	{
		protected internal Type type;

		public TypeAdapter(Type type)
		{
			this.type = type;
		}

		public virtual object Parse(string s)
		{
			return Parse(s, type);	
		}

		private object Parse(string s, Type type)
		{
			if (type.IsAssignableFrom(typeof (string)))
				return s;
			if (type.IsArray)
				return ParseArray(s, type);

			BindingFlags flags = BindingFlags.Static | BindingFlags.FlattenHierarchy | BindingFlags.IgnoreCase | BindingFlags.Public;

			MethodInfo parseMethod = type.GetMethod("Parse", flags, null, new Type[] {typeof (string)}, null);

			if (parseMethod != null && parseMethod.ReturnType == type)
				return parseMethod.Invoke(null, new object[] {s});
			else
				throw new ApplicationException("Can't parse " + type.FullName + " because it doesn't have a static Parse() method");
		}

		private Array ParseArray(string s, Type type)
		{
			string[] strings = s.Split(new char[] {','});

			Array result = Array.CreateInstance(type.GetElementType(), strings.Length);
			for (int i = 0; i < strings.Length; i++)
				result.SetValue(Parse(strings[i], type.GetElementType()), i);

			return result;
		}

		public static bool AreEqual(object o1, object o2)
		{
			if (o1 == null)
				return o2 == null;
			if (o1 is Array && o2 is Array)
				return ArraysAreEqual(o1, o2);
			else
				return (o1.Equals(o2));
		}

		private static bool ArraysAreEqual(object o1, object o2)
		{
			Array a1 = (Array) o1;
			Array a2 = (Array) o2;
			if (a1.Length != a2.Length)
				return false;
			for (int i = 0; i < a1.Length; i++)
			{
				if (!AreEqual(a1.GetValue(i), a2.GetValue(i)))
					return false;
			}
			return true;
		}
	}
}