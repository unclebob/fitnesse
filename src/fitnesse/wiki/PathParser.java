// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.regex.Pattern;
import java.util.Iterator;
import fitnesse.wikitext.widgets.WikiWordWidget;
import fitnesse.util.StringUtil;

public class PathParser
{
	public static final String PATH_SEPARATOR = ".";
	private static final Pattern wikiWordPattern = Pattern.compile(WikiWordWidget.REGEXP);
	private WikiPagePath path;

	public static WikiPagePath parse(String pathName)
	{
		return new PathParser().makePath(pathName);
	}

	private WikiPagePath makePath(String pathName)
	{
		path = new WikiPagePath();
		if(pathName.equals(""))
		{
			return path;
		}
		else if(pathName.equals("root") || pathName.equals(PATH_SEPARATOR))
		{
			path.addName(WikiPagePath.ROOT);
			return path;
		}
		else
		{
			return parsePathName(pathName);
		}
	}

	private WikiPagePath parsePathName(String pathName)
	{
		if(pathName.startsWith(PATH_SEPARATOR))
		{
			path.addName(WikiPagePath.ROOT);
			pathName = pathName.substring(1);
		}
		String[] names = pathName.split("\\" + PATH_SEPARATOR);
		for(int i = 0; i < names.length; i++)
		{
			String pageName = names[i];
			if(nameIsValid(pageName))
				path.addName(pageName);
			else
				return null;
		}
		return path;
	}

	private static boolean nameIsValid(String name)
	{
		return wikiWordPattern.matcher(name).matches();
	}

	public static String render(WikiPagePath path)
	{
		StringBuffer renderedPath = new StringBuffer();
		Iterator i = path.getNames().iterator();
		if(i.hasNext())
		{
			String name = (String) i.next();
			if(!name.equals(WikiPagePath.ROOT))
				renderedPath.append(name);
			else if(!i.hasNext())
				renderedPath.append(PATH_SEPARATOR);
		}
		while(i.hasNext())
		{
			renderedPath.append(PATH_SEPARATOR).append(i.next());
		}
		return renderedPath.toString();
	}
}
