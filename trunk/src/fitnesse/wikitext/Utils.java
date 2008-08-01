// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

public class Utils
{

	private static final String[] specialHtmlChars = new String[]{"&", "<", ">"};
	private static final String[] specialHtmlEscapes = new String[]{"&amp;", "&lt;", "&gt;"};

	public static String escapeText(String value)
	{
		for(int i = 0; i < specialHtmlChars.length; i++)
			value = value.replaceAll(specialHtmlChars[i], specialHtmlEscapes[i]);
		return value;
	}
}
