// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
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
