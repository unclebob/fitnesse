// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.*;

import java.lang.reflect.Method;
import java.util.Stack;

public class LoopingActionFixture extends ActionFixture
{

	Stack loopContexts = new Stack();
	Parse rows;

	boolean isSpecialName(String name)
	{
		return name.equals("do") || name.equals("while");
	}

	Method getAction(String name) throws SecurityException, NoSuchMethodException
	{
		String methodName = isSpecialName(name) ? ("action_" + name) : name;
		return getClass().getMethod(methodName, empty);
	}

	public void doRows(Parse rows)
	{
		this.rows = rows;
		while(this.rows != null)
		{
			doRow(this.rows);
			this.rows = this.rows.more;
		}
	}

	public void doCells(Parse cells)
	{
		this.cells = cells;
		try
		{
			Method action = getAction(cells.text());
			action.invoke(this);
		}
		catch(Exception e)
		{
			exception(cells, e);
		}
	}

	public void action_do()
	{
		loopContexts.push(rows);
	}

	public void action_while() throws Exception
	{
		String methodName = cells.more.text();
		Method action = actor.getClass().getMethod(methodName);
		Boolean result = (Boolean) action.invoke(actor);
		if(result.booleanValue())
		{
			rows = (Parse) loopContexts.peek();
		}
		else
		{
			loopContexts.pop();
		}
	}
}

