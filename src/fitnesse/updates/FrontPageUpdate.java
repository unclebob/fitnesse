// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.*;

public class FrontPageUpdate implements Update
{
	private Updater updater;

	public FrontPageUpdate(Updater updater)
	{
		this.updater = updater;
	}

	public String getName()
	{
		return "FrontPageUpdate";
	}

	public String getMessage()
	{
		return "Creating FrontPage";
	}

	public boolean shouldBeApplied() throws Exception
	{
		return !updater.getRoot().hasChildPage("FrontPage");
	}

	public void doUpdate() throws Exception
	{
		WikiPage frontPage = updater.getRoot().getPageCrawler().addPage(updater.getRoot(), PathParser.parse("FrontPage"));
		PageData data = new PageData(frontPage);
		data.setContent(content);
		frontPage.commit(data);
	}

	private static String content = "\n\n\n" +
		"!c !3 Welcome to the Wonderful World of !-FitNesse-!!";
}
