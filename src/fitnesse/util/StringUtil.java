// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import java.util.*;

public class StringUtil
{
	public static String join(List strings, String delimiter)
	{
		if(strings.isEmpty())
			return "";

		Iterator i = strings.iterator();
		StringBuffer joined = new StringBuffer((String) i.next());

		while(i.hasNext())
		{
			String eachLine = (String) i.next();
			joined.append(delimiter);
			joined.append(eachLine);
		}

		return joined.toString();
	}

	public static String[] combineArrays(String[] first, String[] second)
	{
		List<String> combinedList = new LinkedList();
		combinedList.addAll(Arrays.asList(first));
		combinedList.addAll(Arrays.asList(second));
		return combinedList.toArray(new String[combinedList.size()]);
	}
}
