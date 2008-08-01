// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import java.util.Iterator;

public class TagGroup extends HtmlTag
{
	public TagGroup()
	{
		super("group");
	}

	public String html(int depth) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		for(Iterator iterator = childTags.iterator(); iterator.hasNext();)
		{
			HtmlElement element = (HtmlElement) iterator.next();
			if(element instanceof HtmlTag)
				buffer.append(((HtmlTag) element).html(depth));
			else
				buffer.append(element.html());
		}
		return buffer.toString();
	}
}
