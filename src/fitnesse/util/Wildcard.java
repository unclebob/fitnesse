// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import java.io.*;

public class Wildcard implements FilenameFilter
{
	private String pattern;
	private String prefix;
	private String sufix;
	private int length;

	public Wildcard(String pattern)
	{
  	int starIndex = pattern.indexOf("*");
		if(starIndex > -1)
		{
			prefix = pattern.substring(0, starIndex);
			sufix = pattern.substring(starIndex + 1);
			length = prefix.length() + sufix.length();
		}
		else
		{
			this.pattern = pattern;
		}
	}

	public boolean accept(File dir, String name)
	{
		if(pattern != null)
			return pattern.equals(name);

		boolean goodLength = name.length() >= length;
		boolean goodPrefix = name.startsWith(prefix);
		boolean goodSufix = name.endsWith(sufix);

		return goodLength && goodPrefix && goodSufix;
	}
}
