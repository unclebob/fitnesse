// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fit
{
	public interface Accessor
	{
		object Get(Fixture fixture);
		void Set(Fixture fixture, object value);
		TypeAdapter TypeAdapter{ get; }
		bool AccessesMethodWithAtLeastOneParameter();
	}
}
