// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Reflection;

namespace fit
{
	public class FieldAccessor : AbstractAccessor
	{
		private FieldInfo fieldInfo;

		public FieldAccessor(FieldInfo fieldInfo) : base(fieldInfo.FieldType)
		{
			this.fieldInfo = fieldInfo;
		}

		public override object Get(Fixture fixture)
		{
			return fieldInfo.GetValue(fixture.GetTargetObject());
		}

		public override void Set(Fixture fixture, object value)
		{
			fieldInfo.SetValue(fixture.GetTargetObject(), EnsureCorrectType(fieldInfo.GetType(), value));
		}
	}
}