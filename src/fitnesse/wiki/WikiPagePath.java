// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.util.StringUtil;
import java.util.*;
import java.io.Serializable;

public class WikiPagePath implements Comparable, Cloneable, Serializable
{
	public static final String ROOT = "_root";
	private LinkedList names = new LinkedList();

	public WikiPagePath()
	{
	}

	protected Object clone() throws CloneNotSupportedException
	{
		WikiPagePath clone = new WikiPagePath();
		clone.names = (LinkedList) names.clone();
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
		for(WikiPagePath p = path; !p.isEmpty(); p = p.getRest())
			addName(p.getFirst());
	}

	private WikiPagePath(List names)
	{
		this.names = new LinkedList(names);
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

	public List getNames()
	{
		return names;
	}

	public String toString()
	{
		return "(" + StringUtil.join(names, ".") + ")";
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
		return (!isEmpty() && ROOT.equals(getFirst()));
	}

	public void makeAbsolute()
	{
		if(!isAbsolute())
			addNameToFront(ROOT);
	}

	public int hashCode()
	{
		return StringUtil.join(names, "").hashCode();
	}

	public WikiPagePath relativePath()
	{
		if(isAbsolute())
			return getRest();
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
			return this.names.equals(that.names);
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
		Iterator thatIterator = that.names.iterator();
		while(thatIterator.hasNext())
		{
			Object thisNext = thisIterator.next();
			Object thatNext = thatIterator.next();
			if(!thisNext.equals(thatNext))
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
}
