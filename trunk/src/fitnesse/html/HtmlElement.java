// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

public abstract class HtmlElement
{
	public static final String endl = System.getProperty("line.separator");

	public abstract String html() throws Exception;
}
