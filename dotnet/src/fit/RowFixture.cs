// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;

namespace fit
{
	public abstract class RowFixture : BoundFixture
	{
		public override void DoRows(Parse rows)
		{
			Bind(rows.Parts);
			ArrayList queryResults = new ArrayList(Query());
			Parse row = rows;
			while ((row = row.More) != null)
			{
				object match = FindMatchingObject(queryResults, row);
				if (match == null)
				{
					MarkRowAsMissing(row);
				}
				else
				{
					EvaluateCellsInMatchingRow(row, match);
					queryResults.Remove(match);
				}
			}
			AddSurplusRows(rows, queryResults);
		}

		protected override void Bind(Parse headerCells)
		{
			ColumnBindings = new Binding[headerCells.Size];
			for (int i = 0; headerCells != null; i++, headerCells = headerCells.More)
				ColumnBindings[i] = BindCheckOperation(headerCells);
		}

		private Binding BindCheckOperation(Parse headerCells)
		{
			return CreateBinding(this, headerCells.Text, GetTargetClass(), OperationType.Check);
		}

		private void EvaluateCellsInMatchingRow(Parse row, object match)
		{
			SetTargetObject(match);
			Parse cell = row.Parts;
			foreach (Binding binding in ColumnBindings)
			{
				binding.HandleCell(this, cell);
				cell = cell.More;
			}
		}

		private void AddSurplusRows(Parse rows, ArrayList remaining)
		{
			foreach (object obj in remaining)
				AddSurplusRow(rows, obj);
		}

		private void AddSurplusRow(Parse rows, object extraObject)
		{
			Parse cell = null;
			SetTargetObject(extraObject);
			foreach (Binding binding in ColumnBindings)
			{
				Parse newCell = new Parse("td", null, null, null);
				binding.HandleCell(this, newCell, OperationType.Surplus);
				if (cell == null)
					cell = newCell;
				else
					cell.Last.More = newCell;
			}
			AddRowToTable(cell, rows);
			MarkRowAsSurplus(rows.Last);
		}

		private void AddRowToTable(Parse cells, Parse rows)
		{
			rows.Last.More = new Parse("tr", null, cells, null);
		}

		private object FindMatchingObject(ArrayList queryItems, Parse row)
		{
			return FindMatchingObject(queryItems, row, 0);
		}

		private object FindMatchingObject(ArrayList queryItems, Parse row, int col)
		{
			if (!ColumnHasBinding(col))
				return null;
			ArrayList matches = new ArrayList();
			foreach (object queryItem in queryItems)
			{
				SetTargetObject(queryItem);
				if (IsMatch(row, col))
					matches.Add(queryItem);
			}
			if (UniqueMatchFound(matches))
				return UniqueMatch(matches);
			else if (matches.Count > 0 && !ColumnHasBinding(col + 1))
				return matches[0];
			else 
				return FindMatchingObject(queryItems, row, col + 1);
		}

		private bool IsMatch(Parse row, int col)
		{
			return CellOperation.Evaluate(this, ColumnBindings[col].MemberName, GetCellForColumn(row, col));
		}

		private Parse GetCellForColumn(Parse row, int col)
		{
			Parse cell = row.Parts;
			for (int i = 0; i < col; i++)
				cell = cell.More;
			return cell;
		}

		private bool ColumnHasBinding(int col)
		{
			return col < ColumnBindings.Length && ColumnBindings[col] != null;
		}

		private bool UniqueMatchFound(ArrayList matches)
		{
			return matches.Count == 1;
		}

		private object UniqueMatch(ArrayList matches)
		{
			return matches[0];
		}

		private void MarkRowAsMissing(Parse row)
		{
			Parse cell = row.Parts;
			cell.AddToBody(Label("missing"));
			Wrong(cell);
		}

		private void MarkRowAsSurplus(Parse row)
		{
			Wrong(row.Parts);
			row.Parts.AddToBody(Label("surplus"));
		}

		public override object GetTargetObject()
		{
			return targetObject;
		}

		private void SetTargetObject(object obj)
		{
			targetObject = obj;
		}

		public abstract object[] Query(); // get rows to be compared
		public abstract Type GetTargetClass(); // get expected type of row

		private object targetObject = null;
	}
}