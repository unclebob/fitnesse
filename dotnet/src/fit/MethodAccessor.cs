// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Reflection;

namespace fit
{
	public class MethodAccessor : AbstractAccessor
	{
		protected internal MethodInfo methodInfo;

		public MethodAccessor(MethodInfo method) : base(method.ReturnType)
		{
			this.methodInfo = method;
		}

		public override object Get(Fixture fixture)
		{
			if (methodInfo.Name == "ToString")
			{
				return fixture.GetTargetObject().ToString();
			}
			return methodInfo.Invoke(fixture.GetTargetObject(), new object[] {});
		}

		public override void Set(Fixture fixture, object value)
		{
			if (value == null)
			{
				if (methodInfo.GetParameters().Length == 1)
				{
					methodInfo.Invoke(fixture.GetTargetObject(), new object[] {null});
				}
				else
				{
					methodInfo.Invoke(fixture.GetTargetObject(), new object[] {});
				}
			}
			else
			{
				object[] args = {
					EnsureCorrectType(methodInfo.GetParameters()[0].ParameterType,
					                  value,
					                  new TypeAdapter(methodInfo.GetParameters()[0].ParameterType))
				};
				methodInfo.Invoke(fixture.GetTargetObject(), args);
			}
		}

		public override bool AccessesMethodWithAtLeastOneParameter()
		{
			return methodInfo.GetParameters().Length > 0;
		}
	}
}