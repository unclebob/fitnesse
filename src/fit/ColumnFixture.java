// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

public class ColumnFixture extends Fixture
{

  protected Binding columnBindings[];
  protected boolean hasExecuted = false;

  // Traversal ////////////////////////////////

  public void doRows(Parse rows)
  {
    bind(rows.parts);
    super.doRows(rows.more);
  }

  public void doRow(Parse row)
  {
    hasExecuted = false;
    try
    {
      reset();
      super.doRow(row);
      if(!hasExecuted)
      {
        execute();
      }
    }
    catch(Exception e)
    {
      exception(row.leaf(), e);
    }
  }

  public void doCell(Parse cell, int column)
  {
    try
    {
	    columnBindings[column].doCell(this, cell);
    }
    catch(Throwable e)
    {
      exception(cell, e);
    }
  }

  public void check(Parse cell, TypeAdapter a)
  {
		try
		{
	    executeIfNeeded(); 
		}
		catch(Exception e)
		{
			exception(cell, e);
		}
	  super.check(cell, a);
  }

	protected void executeIfNeeded() throws Exception
	{
		if(!hasExecuted)
		{
				hasExecuted = true;
				execute();
		}
	}

	public void reset() throws Exception
  {
    // about to process first cell of row
  }

  public void execute() throws Exception
  {
    // about to process first method call of row
  }

  // Utility //////////////////////////////////

  protected void bind(Parse heads)
  {
	  try
	  {
		  columnBindings = new Binding[heads.size()];
		  for(int i = 0; heads != null; i++, heads = heads.more)
		  {
		    String name = heads.text();
			  columnBindings[i] = Binding.create(this, name);
		  }
	  }
	  catch(Throwable throwable)
	  {
		  exception(heads, throwable);
	  }
  }

}
