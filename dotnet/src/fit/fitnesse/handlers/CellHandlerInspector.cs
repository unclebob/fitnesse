// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.handlers
{
	public class CellHandlerInspector : RowFixture
	{
		public override object[] Query()
		{
			object[] handlers = new object[CellOperation.Handlers.Count];
			CellOperation.Handlers.CopyTo(handlers, 0);
			return handlers;
		}

		public override Type GetTargetClass()
		{
			return typeof(ICellHandler);
		}
	}
}
