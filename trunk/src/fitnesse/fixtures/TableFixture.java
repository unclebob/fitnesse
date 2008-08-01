// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.*;
import fit.exception.FitFailureException;

public abstract class TableFixture extends Fixture
{
	protected Parse firstRow;

	public void doRows(Parse rows)
	{
		firstRow = rows;
		if(rows == null)
			throw new FitFailureException("There are no rows in this table");
		doStaticTable(rows.size());
	}

	protected abstract void doStaticTable(int rows);

	protected Parse getCell(int row, int column)
	{
		return firstRow.at(row, column);
	}

	protected String getText(int row, int column)
	{
		return getCell(row, column).text();
	}

	protected boolean blank(int row, int column)
	{
		return getText(row, column).equals("");
	}

	protected void wrong(int row, int column)
	{
		wrong(getCell(row, column));
	}

	protected void right(int row, int column)
	{
		right(getCell(row, column));
	}

	protected void wrong(int row, int column, String actual)
	{
		wrong(getCell(row, column), actual);
	}

	protected void ignore(int row, int column)
	{
		ignore(getCell(row, column));
	}

	protected int getInt(int row, int column)
	{
		int i = 0;
		String text = getText(row, column);
		if(text.equals(""))
		{
			ignore(row, column);
			return 0;
		}
		try
		{
			i = Integer.parseInt(text);
		}
		catch(NumberFormatException e)
		{
			wrong(row, column);
		}
		return i;
	}
}
