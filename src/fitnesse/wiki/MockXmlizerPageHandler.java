// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class MockXmlizerPageHandler implements XmlizerPageHandler
{
	public List adds = new LinkedList();
	public int exits = 0;

	public void pageAdded(WikiPage newPage) throws Exception
	{
		adds.add(newPage.getName());
	}

	public void exitPage()
	{
		exits++;
	}
}
