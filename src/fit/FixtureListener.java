// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public interface FixtureListener
{
	public void tableFinished(Parse table);
	public void tablesFinished(Counts count);
}
