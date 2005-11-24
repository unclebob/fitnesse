using fit;
// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fitnesse.fixtures
{
	public abstract class TableFixture : Fixture
	{
		private Parse rows;
		
		protected abstract void DoStaticTable(int rows);

		public override void DoRows(Parse rows)
		{
			this.rows = rows;
			DoStaticTable(rows.Size);
		}

		protected Parse GetCell(int row, int column)
		{
			return rows.At(row, column);
		}

		protected string GetString(int row, int column)
		{
			Parse cell = GetCell(row, column);
			if (cell == null)
			{
				return null;
			}
			return cell.Text;
		}

		protected void Right(int row, int column)
		{
			Parse cell = rows.At(row, column);
			Right(cell);
		}

		protected void Wrong(int row, int column)
		{
			Parse cell = rows.At(row, column);
			Wrong(cell);
		}

		protected void Wrong(int row, int column, string actual)
		{
			Parse cell = rows.At(row, column);
			Wrong(cell, actual);
		}

		protected void Ignore(int row, int column)
		{
			Parse cell = rows.At(row, column);
			Ignore(cell);
		}

		protected int GetInt(int row, int column)
		{
			TypeAdapter adapter = new TypeAdapter(typeof(int));
			return (int) adapter.Parse(GetString(row, column));
		}

		protected bool Blank(int row, int column)
		{
			Parse cell = rows.At(row, column);
			return "".Equals(cell.Text.Trim());
		}
	}
}
