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
