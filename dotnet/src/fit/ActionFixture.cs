// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;

namespace fit
{
	public class ActionFixture : Fixture
	{
		private Parse cells;
		private Fixture actor;
		private object targetObject;

		// Traversal ////////////////////////////////

		public override void DoCells(Parse cells)
		{
			this.cells = cells;
			try
			{
				targetObject = this;
				Accessor accessor = AccessorFactory.Create(typeof(ActionFixture), cells.Text);
				accessor.Set(this, null);
				targetObject = actor;
			}
			catch (Exception e)
			{
				Exception(cells, e);
			}
		}

		// Actions //////////////////////////////////

		public virtual void Start()
		{
			actor = LoadFixture(cells.More.Text);
			actor.Counts = this.Counts;
		}

		public virtual void Enter()
		{
			CellOperation.Input(actor, cells.More.Text, cells.More.More);
		}

		public virtual void Press()
		{
			CellOperation.Execute(actor, cells.More.Text, cells.More);
		}

		public virtual void Check()
		{
			CellOperation.Check(actor, cells.More.Text, cells.More.More);
		}

		public override object GetTargetObject() {
			return targetObject;
		}
	}
}