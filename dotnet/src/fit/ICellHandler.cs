// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;

namespace fit
{
	public interface ICellHandler
	{
		string Name { get; }
		bool Match(string searchString, Type type);
		void HandleInput(Fixture fixture, Parse cell, Accessor accessor);
		void HandleCheck(Fixture fixture, Parse cell, Accessor accessor);
		void HandleExecute(Fixture fixture, Parse cell, Accessor accessor);
		bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor);
	}
}