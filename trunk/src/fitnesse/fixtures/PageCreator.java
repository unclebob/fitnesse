// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;
import fitnesse.wiki.*;

import java.util.StringTokenizer;

public class PageCreator extends ColumnFixture
{
	public String pageName;
	public String pageContents;
	public String pageAttributes = "";

	public boolean valid() throws Exception
	{
		try
		{
			WikiPage root = FitnesseFixtureContext.root;
			WikiPagePath pagePath = PathParser.parse(pageName);
			WikiPage thePage = root.getPageCrawler().addPage(root, pagePath, pageContents);
			PageData data = thePage.getData();
			setAttributes(data);
			thePage.commit(data);
			pageAttributes = "";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	private void setAttributes(PageData data) throws Exception
	{
		StringTokenizer tokenizer = new StringTokenizer(pageAttributes, ",");
		while(tokenizer.hasMoreTokens())
		{
			String nameValuePair = tokenizer.nextToken();
			int equals = nameValuePair.indexOf("=");
			if(equals < 0)
				throw new Exception("Attribute must have form name=value");
			String name = nameValuePair.substring(0, equals);
			String value = nameValuePair.substring(equals + 1);
			data.setAttribute(name, value);
		}
	}
}

