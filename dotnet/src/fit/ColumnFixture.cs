// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text.RegularExpressions;

namespace fit
{
	public abstract class ColumnFixture : BoundFixture
	{
		public override void DoRows(Parse rows)
		{
			Bind(rows.Parts);
			base.DoRows(rows.More);
		}

		protected override void Bind(Parse headerCells)
		{
			ColumnBindings = new Binding[headerCells.Size];
			for (int i = 0; headerCells != null; i++, headerCells = headerCells.More)
			{
				ColumnBindings[i] = CreateBinding(this, headerCells.Text, GetType());
			}
		}
	}
}