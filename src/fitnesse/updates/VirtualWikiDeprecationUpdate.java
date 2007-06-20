// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.*;
import fitnesse.wikitext.*;
import fitnesse.wikitext.widgets.*;

public class VirtualWikiDeprecationUpdate extends PageTraversingUpdate
{
	public static WidgetBuilder virtualWidgetBuilder = new WidgetBuilder(new Class[]{VirtualWikiWidget.class});

	public VirtualWikiDeprecationUpdate(Updater updater)
	{
		super(updater);
	}

	public String getMessage()
	{
		return "Updating pages with !virtualwiki widgets";
	}

	public String getName()
	{
		return "VirtualWikiDeprecationUpdate";
	}

	public void processPage(WikiPage page) throws Exception
	{
		PageData data = page.getData();
		WidgetRoot widgetRoot = new WidgetRoot(data.getContent(), page, virtualWidgetBuilder);
		while(widgetRoot.hasNextChild())
		{
			WikiWidget widget = widgetRoot.nextChild();
			if(widget instanceof VirtualWikiWidget)
			{
				VirtualWikiWidget vWidget = (VirtualWikiWidget) widget;
				String url = vWidget.getRemoteUrl();
				data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, url);
				page.commit(data);
				break;
			}
		}
	}
}
