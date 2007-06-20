// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.util.StringUtil;
import static fitnesse.wiki.WikiPagePath.Mode.*;

import java.io.Serializable;
import java.util.*;

public class WikiPagePath implements Comparable, Cloneable, Serializable
{
	public enum Mode
	{
		ABSOLUTE, SUB_PAGE, BACKWARD_SEARCH, RELATIVE
	}

	private LinkedList<String> names = new LinkedList<String>();
	private Mode mode = RELATIVE;

	public WikiPagePath()
	{
	}

	public WikiPagePath(String[] names)
	{
		for(int i = 0; i < names.length; i++)
			addName(names[i]);
	}

	protected Object clone() throws CloneNotSupportedException
	{
		WikiPagePath clone = new WikiPagePath();
		clone.names = (LinkedList<String>) names.clone();
		clone.mode = mode;
		return clone;
	}

	public WikiPagePath copy()
	{
		try
		{
			return (WikiPagePath) clone();
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

	public WikiPagePath(WikiPage page) throws Exception
	{
		PageCrawler crawler = page.getPageCrawler();
		while(!crawler.isRoot(page))
		{
			names.addFirst(page.getName());
			page = page.getParent();
		}
	}

	public WikiPagePath(WikiPagePath path)
	{
		mode = path.mode;
		for(WikiPagePath p = path; !p.isEmpty(); p = p.getRest())
			addName(p.getFirst());
	}

	private WikiPagePath(List<String> names)
	{
		this.names = new LinkedList<String>(names);
	}

	public String getFirst()
	{
		return isEmpty() ? null : (String) names.get(0);
	}

	public WikiPagePath addName(String name)
	{
		names.add(name);
		return this;
	}

	public WikiPagePath addNameToFront(String name)
	{
		names.addFirst(name);
		return this;
	}

	public WikiPagePath getRest()
	{
		int size = names.size();
		return (size <= 1) ? new WikiPagePath() : new WikiPagePath(names.subList(1, size));
	}

	public boolean isEmpty()
	{
		return names.size() == 0;
	}

	public String last()
	{
		return (String) (names.size() == 0 ? null : names.get(names.size() - 1));
	}

	public List<String> getNames()
	{
		return names;
	}

	public String toString()
	{
		String prefix = "";
		if(mode == ABSOLUTE) prefix = ".";
		else if(mode == SUB_PAGE) prefix = ">";
		else if(mode == BACKWARD_SEARCH) prefix = "<";
		return "(" + prefix + StringUtil.join(names, ".") + ")";
	}

	public void pop()
	{
		if(names.size() > 0)
			names.removeLast();
	}

	public WikiPagePath append(WikiPagePath childPath)
	{
		WikiPagePath newPath = new WikiPagePath(this);
		for(WikiPagePath p = childPath; !p.isEmpty(); p = p.getRest())
			newPath.addName(p.getFirst());
		return newPath;
	}

	public boolean isAbsolute()
	{
		return (mode == ABSOLUTE);
	}

	public void makeAbsolute()
	{
		mode = ABSOLUTE;
	}

	public int hashCode()
	{
		return StringUtil.join(names, "").hashCode();
	}

	public WikiPagePath relativePath()
	{
		if(isAbsolute())
		{
			WikiPagePath relativePath = new WikiPagePath(this);
			relativePath.setPathMode(RELATIVE);
			return relativePath;
		}
		else
			return this;
	}

	public int compareTo(Object o)
	{
		if(o instanceof WikiPagePath)
		{
			WikiPagePath p = (WikiPagePath) o;
			String compressedName = StringUtil.join(names, "");
			String compressedArgumentName = StringUtil.join(p.names, "");
			return compressedName.compareTo(compressedArgumentName);
		}
		return 1; // we are greater because we are the right type.
	}

	public boolean equals(Object o)
	{
		if(o instanceof WikiPagePath)
		{
			WikiPagePath that = (WikiPagePath) o;
			return mode == that.mode && this.names.equals(that.names);
		}
		return false;
	}

	public WikiPagePath parentPath()
	{
		WikiPagePath parentPath = new WikiPagePath(this);
		parentPath.pop();
		return parentPath;
	}

	public boolean startsWith(WikiPagePath that)
	{
		if(that.names.size() > names.size())
			return false;

		Iterator thisIterator = names.iterator();
		for(String name : that.names)
		{
			Object thisNext = thisIterator.next();
			if(!thisNext.equals(name))
				return false;
		}
		return true;
	}

	public WikiPagePath withNameAdded(String name)
	{
		WikiPagePath path = new WikiPagePath(this);
		path.addName(name);
		return path;
	}

	public WikiPagePath subtractFromFront(WikiPagePath operand)
	{
		WikiPagePath difference = new WikiPagePath(this);
		if(difference.startsWith(operand))
		{
			difference.setPathMode(Mode.RELATIVE);
			for(String name : operand.getNames())
			{
				if(name.equals(difference.getFirst()))
					difference.names.removeFirst();
				else
					break;
			}
		}
		return difference;
	}

	public void setPathMode(Mode mode)
	{
		this.mode = mode;
	}

	public boolean isSubPagePath()
	{
		return mode == SUB_PAGE;
	}

	public boolean isBackwardSearchPath()
	{
		return mode == BACKWARD_SEARCH;
	}

}
