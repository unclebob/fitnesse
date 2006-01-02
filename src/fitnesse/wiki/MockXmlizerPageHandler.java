// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class MockXmlizerPageHandler implements XmlizerPageHandler
{
	public List<String> handledPages = new LinkedList<String>();
	public List<Date> modDates = new LinkedList<Date>();
	public int exits = 0;

	public void enterChildPage(WikiPage newPage, Date lastModified) throws Exception
	{
		handledPages.add(newPage.getName());
		modDates.add(lastModified);
	}

	public void exitPage()
	{
		exits++;
	}
}
