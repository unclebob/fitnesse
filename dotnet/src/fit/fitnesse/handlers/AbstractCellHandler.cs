// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.handlers
{
	public abstract class AbstractCellHandler : ICellHandler
	{
		public string Name
		{
			get { return GetType().Name; }
		}

		public virtual bool Match(string searchString, Type type)
		{
			return false;
		}

		public virtual void HandleInput(Fixture fixture, Parse cell, Accessor accessor)
		{
			accessor.Set(fixture, cell.Text);
		}

		public virtual void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (HandleEvaluate(fixture, cell, accessor))
			{
				fixture.Right(cell);
			}
			else
			{
				if (accessor.Get(fixture) == null)
				{
					fixture.Wrong(cell, "null");
				}
				else
				{
					fixture.Wrong(cell, accessor.Get(fixture).ToString());
				}
			}
		}

		public virtual void HandleExecute(Fixture fixture, Parse cell, Accessor accessor)
		{
			accessor.Set(fixture, null);
		}

		public virtual bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			object expected = accessor.TypeAdapter.Parse(cell.Text);
			object actual = GetActual(accessor, fixture);
			return TypeAdapter.AreEqual(expected, actual);
		}

		protected virtual object GetActual(Accessor accessor, Fixture fixture) {
			return accessor.Get(fixture);
		}
	}
}