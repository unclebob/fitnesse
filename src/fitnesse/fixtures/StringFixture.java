// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.*;

public class StringFixture extends ColumnFixture
{
	public int subStringSize = 10;
	public String value;
	private String expected;

	public void check(Parse cell, TypeAdapter a)
	{
		expected = cell.text();
		super.check(cell, a);
	}

  public void execute() throws Exception
  {
    value = value.trim();
  }

	public String startsWith()
	{
		if(value.startsWith(expected))
			return expected;
		else
		{
			if(value.length() <= subStringSize)
				return value;
			else
				return value.substring(0, subStringSize) + "...";
		}
	}

	public String endsWith()
	{
		if(value.endsWith(expected))
			return expected;
		else
		{
			if(value.length() <= subStringSize)
				return value;
			else
				return "..." + value.substring(value.length() - subStringSize);
		}
	}

	public String contains()
	{
  	if(value.indexOf(expected) != -1)
		  return expected;
		else
	  {
		  if(value.length() <= subStringSize)
			  return value;
		  else
			  return "...";
	  }
	}
}
