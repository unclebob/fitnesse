// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Reflection;

namespace fit
{
	public class MethodAccessor : AbstractAccessor
	{
		private object retrievedValue;

		protected internal MethodInfo methodInfo;

		public MethodAccessor(MethodInfo method) : base(method.ReturnType)
		{
			this.methodInfo = method;
		}

		public override object Get(Fixture fixture)
		{
			if (this.retrievedValue == null)
			{
				if (methodInfo.Name == "ToString")
				{
					this.retrievedValue = fixture.GetTargetObject().ToString();
				}
				else
				{
					this.retrievedValue = methodInfo.Invoke(fixture.GetTargetObject(), new object[] {});
				}
			}
			return this.retrievedValue;
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