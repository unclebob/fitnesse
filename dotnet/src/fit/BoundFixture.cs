// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text.RegularExpressions;

namespace fit
{
	public abstract class BoundFixture : Fixture
	{
		public Binding[] ColumnBindings;
		public bool HasExecuted = false;

		protected abstract void Bind(Parse headerCells);

		public override void DoRow(Parse row) 
		{
			HasExecuted = false;
			try {
				Reset();
				base.DoRow(row);
				if (!HasExecuted) 
					Execute();
			} 
			catch (Exception e) {
				Exception(row.Leaf, e);
			}
		}

		public override void DoCell(Parse cell, int column) {
			Binding binding = ColumnBindings[column];
			try {
				binding.HandleCell(this, cell);
			} 
			catch(Exception e) {
				Exception(cell, e);
			}
		}

		public void CheckCalled()
		{
			if (!HasExecuted) {
				Execute();
				HasExecuted = true;
			}
		}

		public virtual void Reset() {
			// about to process first cell of row
		}

		public virtual void Execute() {
			// about to process first method call of row
		}

		protected Binding CreateBinding(Fixture fixture, string name, Type targetType)
		{
			return CreateBinding(fixture, name, targetType, GetOperationType(name));
		}

		protected Binding CreateBinding(Fixture fixture, string name, Type targetType, OperationType operationType)
		{
			EnsureTargetTypeNotString(ref targetType);
			EnsureTargetTypeNotNull(fixture, ref targetType);
			return new Binding(name, operationType);
		}

		private void EnsureTargetTypeNotNull(Fixture fixture, ref Type targetType)
		{
			if (targetType == null)
				targetType = fixture.GetType();
		}

		private void EnsureTargetTypeNotString(ref Type targetType)
		{
			if (targetType == typeof (string))
				targetType = null;
		}

		private OperationType GetOperationType(string name)
		{
			if (NoOperationIsImpliedBy(name))
				return OperationType.None;
			if (CheckIsImpliedBy(name))
				return OperationType.Check;
			return OperationType.Input;
		}

		private bool NoOperationIsImpliedBy(string name)
		{
			return "".Equals(name.Trim());
		}

		internal virtual bool CheckIsImpliedBy(string name)
		{
			return Regex.IsMatch(name, "(\\?|!|\\(\\))$");
		}
	}
}
