// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using fit;

namespace fitnesse.acceptanceTests
{
	public class ParameterizedRowFixture : RowFixture
	{
		public override object[] Query()
		{
			ArrayList list = new ArrayList();
			list.Add(new ValueWrapper(new TypeAdapter(typeof(string)).Parse(Args[0])));
			list.Add(new ValueWrapper(new TypeAdapter(typeof(int)).Parse(Args[1])));
			list.Add(new ValueWrapper(new TypeAdapter(typeof(bool)).Parse(Args[2])));
			list.Add(new ValueWrapper(new TypeAdapter(typeof(Person)).Parse(Args[3])));
			return list.ToArray();
		}

		public override Type GetTargetClass()
		{
			return typeof(ValueWrapper);
		}
	}

	public class ValueWrapper
	{
		private object value;

		public ValueWrapper(object value)
		{
			this.value = value;	
		}

		public object Value
		{
			get { return value.ToString(); }
		}
	}
}
