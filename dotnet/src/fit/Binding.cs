// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fit
{
	public class Binding
	{
		public Binding(string columnHeader, OperationType operationType)
		{
			this.memberName = columnHeader;
			this.operationType = operationType;
		}

		public virtual void HandleCell(BoundFixture fixture, Parse cell) {
			HandleCell(fixture, cell, this.operationType);
		}

		public virtual void HandleCell(BoundFixture fixture, Parse cell, OperationType operationType)
		{
			switch (operationType) {
				case OperationType.Input:
					CellOperation.Input(fixture, memberName, cell);
					break;
				case fit.OperationType.Check:
					fixture.CheckCalled();
					CellOperation.Check(fixture, memberName, cell);
					break;
				case fit.OperationType.Execute:
					CellOperation.Execute(fixture, memberName, cell);
					break;
				case fit.OperationType.Surplus:
					CellOperation.Surplus(fixture, memberName, cell);
					break;
			}
		}

		public string MemberName
		{
			get { return memberName; }
		}

		private OperationType operationType;
		private string memberName;
	}
}