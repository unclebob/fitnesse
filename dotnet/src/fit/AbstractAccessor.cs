// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;

namespace fit
{
	public abstract class AbstractAccessor : Accessor
	{
		protected TypeAdapter adapter;

		public AbstractAccessor(Type type)
		{
			this.adapter = new TypeAdapter(type);
		}

		public abstract object Get(Fixture fixture);
		public abstract void Set(Fixture fixture, object value);

		public TypeAdapter TypeAdapter
		{
			get { return this.adapter; }
		}

		protected object EnsureCorrectType(Type type, object value, TypeAdapter adapter) {
			if (value == null)
				return null;
			if (value.GetType().Equals(typeof(string)))
				return adapter.Parse((string)value);
			return value;
		}

		protected object EnsureCorrectType(Type type, object value) {
			return EnsureCorrectType(type, value, adapter);
		}

		public virtual bool AccessesMethodWithAtLeastOneParameter() {
			return false;
		}
	}
}