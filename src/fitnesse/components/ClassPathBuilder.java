// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.util.Wildcard;
import fitnesse.wiki.*;

import java.io.File;
import java.util.*;

public class ClassPathBuilder extends InheritedItemBuilder
{
	public String getClasspath(WikiPage page) throws Exception
	{
		List paths = getInheritedPathElements(page, new HashSet(89));
		String classPathString = createClassPathString(paths, getPathSeparator(page));
		return classPathString;
	}

	public String getPathSeparator(WikiPage page) throws Exception
	{
		String separator = page.getData().getVariable("PATH_SEPARATOR");
		if(separator == null)
			separator = (String) System.getProperties().get("path.separator");

		return separator;
	}

	public List getInheritedPathElements(WikiPage page, Set visitedPages) throws Exception
	{
		return getInheritedItems(page, visitedPages);
	}

	public String createClassPathString(List paths, String separator)
	{
		if(paths.isEmpty())
			return "defaultPath";
		StringBuffer pathsString = new StringBuffer();

		paths = expandWildcards(paths);
		Set addedPaths = new HashSet();
		for(Iterator i = paths.iterator(); i.hasNext();)
		{
			String path = (String) i.next();
			if(path.matches(".*\\s.*") && path.indexOf("\"") == -1)
				path = "\"" + path + "\"";

			if(!addedPaths.contains(path))
			{
				addedPaths.add(path);
				addSeparatorIfNecessary(pathsString, separator);
				pathsString.append(path);
			}
		}
		return pathsString.toString();
	}

	private List expandWildcards(List paths)
	{
		List newPaths = new ArrayList();
		for(Iterator iterator = paths.iterator(); iterator.hasNext();)
		{
			String path = (String) iterator.next();
			File file = new File(path);
			File dir = new File(file.getAbsolutePath()).getParentFile();
			if(file.getName().indexOf('*') != -1 && dir.exists())
			{
				File[] files = dir.listFiles(new Wildcard(file.getName()));
				for(int i = 0; i < files.length; i++)
					newPaths.add(files[i].getPath());
			}
			else
				newPaths.add(path);
		}

		return newPaths;
	}

	private void addSeparatorIfNecessary(StringBuffer pathsString, String separator)
	{
		if(pathsString.length() > 0)
			pathsString.append(separator);
	}

	protected List getItemsFromPage(WikiPage page) throws Exception
	{
		return page.getData().getClasspaths();
	}
}
